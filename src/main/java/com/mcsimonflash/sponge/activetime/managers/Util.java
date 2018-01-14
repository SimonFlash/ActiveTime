package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.TimeWrapper;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Identifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {

    public static final Text prefix = toText("&b[&fActiveTime&b]&r ");
    public static final int[] timeConst = {604800, 86400, 3600, 60, 1};
    public static final String[] unitAbbrev = {"w", "d", "h", "m", "s"};
    public static final Pattern timeFormat = Pattern.compile("(?:([0-9]+)w)?(?:([0-9]+)d)?(?:([0-9]+)h)?(?:([0-9]+)m)?(?:([0-9])+s)?");

    public static void sendMessage(CommandSource src, String message) {
        src.sendMessage(prefix.concat(toText(message)));
    }

    public static void sendPagination(CommandSource src, String title, List<Text> texts) {
        (src instanceof Player ? PaginationList.builder() : PaginationList.builder().linesPerPage(-1))
                .title(toText(title))
                .padding(toText("&b="))
                .contents(texts)
                .build()
                .sendTo(src);
    }

    public static Task createTask(String name, Consumer<Task> consumer, int interval, boolean async) {
        return (async ? Task.builder().async() : Task.builder())
                .name(name).execute(consumer)
                .interval(interval, TimeUnit.MILLISECONDS)
                .submit(ActiveTime.getPlugin());
    }

    public static void initialize() {
        Config.readConfig();
        Storage.readStorage();
    }

    public static void startTasks() {
        if (Storage.updateTask != null) {
            Storage.updateTask.cancel();
        }
        if (Storage.saveTask != null) {
            Storage.saveTask.cancel();
        }
        if (Storage.milestoneTask != null) {
            Storage.milestoneTask.cancel();
        }
        if (Storage.limitTask != null) {
            Storage.limitTask.cancel();
        }
        startUpdateTask();
        startSaveTask();
        if (Config.milestoneInt > 0) {
            startMilestoneTask();
        }
        if (Config.limitInt > 0) {
            startLimitTask();
        }
    }

    public static HoconConfigurationLoader getLoader(Path path, boolean asset) throws IOException {
        try {
            if (Files.notExists(path)) {
                if (asset) {
                    Sponge.getAssetManager().getAsset(ActiveTime.getPlugin(), path.getFileName().toString()).get().copyToFile(path);
                } else {
                    Files.createFile(path);
                }
            }
            return HoconConfigurationLoader.builder().setPath(path).build();
        } catch (IOException e) {
            ActiveTime.getPlugin().getLogger().error("Error loading config file! File:[" + path.getFileName().toString() + "]");
            throw e;
        }
    }

    public static Text toText(String msg) {
        return TextSerializers.FORMATTING_CODE.deserialize(msg);
    }

    public static int parseTime(String timeStr) {
        try {
            return Integer.parseInt(timeStr);
        } catch (NumberFormatException ignored) {
            int time = 0;
            Matcher matcher = timeFormat.matcher(timeStr);
            if (matcher.matches()) {
                for (int i = 1; i <= 5; i++) {
                    time += matcher.group(i) != null ? Integer.parseInt(matcher.group(i)) * timeConst[i - 1] : 0;
                }
            }
            return time;
        }
    }

    public static String printTime(int time) {
        if (time <= 0) {
            return time + "s";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (time / timeConst[i] > 0) {
                builder.append(time / timeConst[i]).append(unitAbbrev[i]);
                time = time % timeConst[i];
            }
        }
        return builder.toString();
    }

    public static String printTime(TimeWrapper time) {
        return "&b" + printTime(time.getActiveTime()) + (ActiveTime.isNucleusEnabled() && time.getActiveTime() + time.getAfkTime() != 0 ? " &f| &b" + printTime(time.getAfkTime()) + " &f- &7" + new DecimalFormat("##.##%").format((double) time.getActiveTime() / (time.getActiveTime() + time.getAfkTime())) : "");
    }

    public static String printDate(LocalDate date) {
        return date.getMonthValue() + "/" + String.format("%02d", date.getDayOfMonth());
    }

    public static void startNameTask(Player player) {
        createTask("ActiveTime UpdateUsername Task (" + player.getUniqueId() + ")", task -> {
            String name = Storage.getUsername(player.getUniqueId());
            if (!player.getName().equals(name) && !Storage.setUsername(player.getUniqueId(), player.getName())) {
                ActiveTime.getPlugin().getLogger().error("Error updating username. | UUID:[" + player.getUniqueId() + "] NewName:[" + player.getName() + "] OldName:[\" + name + \"] ");
            }
        }, 0, true);
    }

    public static void startUpdateTask() {
        Storage.updateTask = ActiveTime.isNucleusEnabled() ? createTask("ActiveTime UpdateTimes Task (Nucleus)", task -> {
            Map<UUID, Boolean> players = Sponge.getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission("activetime.log.base")).collect(Collectors.toMap(Identifiable::getUniqueId, p -> !NucleusIntegration.isPlayerAfk(p)));
            createTask("ActiveTime UpdateTimes Async Processor (Nucleus)", t -> players.forEach((u, a) -> addCachedTime(u, Config.updateInt, a)), 0, true);
        }, Config.updateInt * 1000 - 1, false) : createTask("ActiveTime UpdateTimes Task", task -> {
            List<UUID> players = Sponge.getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission("activetime.log.base")).map(Identifiable::getUniqueId).collect(Collectors.toList());
            createTask("ActiveTime UpdateTimes Async Processor", t -> players.forEach(u -> addCachedTime(u, Config.updateInt, true)), 0, true);
        }, Config.updateInt * 1000 - 1, false);
    }

    public static void addCachedTime(UUID uuid, int time, boolean active) {
        Storage.times.computeIfAbsent(uuid, u -> new TimeWrapper()).add(time, active);
    }

    public static void startSaveTask() {
        Storage.saveTask = createTask("ActiveTime SaveTimes Task", task -> {
            ImmutableMap<UUID, TimeWrapper> times = ImmutableMap.copyOf(Storage.times);
            Storage.times.clear();
            createTask("ActiveTime SaveTimes Async Processor", t -> {
                times.forEach(Util::saveTime);
                Storage.syncCurrentDate();
                Storage.buildLeaderboard();
            }, 0, true);
        }, Config.saveInt * 1000 - 1, false);
    }

    public static void saveTime(UUID uuid, TimeWrapper time) {
        boolean total = Storage.setTotalTime(uuid, Storage.getTotalTime(uuid).add(time));
        boolean daily = Storage.setDailyTime(uuid, Storage.getDailyTime(uuid).add(time));
        if (!daily || !total) {
            ActiveTime.getPlugin().getLogger().error("Error saving time for uuid " + uuid + "!");
        }
    }

    public static void startMilestoneTask() {
        Storage.milestoneTask = createTask("ActiveTime CheckMilestones Task", task -> {
            ImmutableList<Player> players = ImmutableList.copyOf(Sponge.getServer().getOnlinePlayers());
            createTask("ActiveTime CheckMilestones Async Processor", t -> players.forEach(Util::checkMilestones), 0, true);
        }, Config.milestoneInt * 1000 - 1, false);
    }

    public static void checkMilestones(Player player) {
        int activetime = Storage.getTotalTime(player.getUniqueId()).getActiveTime();
        Storage.milestones.forEach(m -> m.process(player, activetime));
    }

    public static void startLimitTask() {
        Storage.limitTask = createTask("ActiveTime CheckLimits Task", task -> {
            ImmutableList<Player> players = ImmutableList.copyOf(Sponge.getServer().getOnlinePlayers());
            createTask("ActiveTime CheckLimits Async Processor", t -> players.forEach(Util::checkLimit), 0, true);
        }, Config.limitInt * 1000 - 1, false);
    }

    public static void checkLimit(Player player) {
        player.getOption("playtime").map(Util::parseTime).filter(i -> i > 0).ifPresent(i -> {
            if (Storage.getDailyTime(player.getUniqueId()).getActiveTime() >= i) {
                createTask("ActiveTime KickPlayer Task", task -> player.kick(toText("You have surpassed your playtime for the day of " + i + "!")), 0, false);
            }
        });
    }

}