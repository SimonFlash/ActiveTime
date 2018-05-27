package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.TimeHolder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    private static final Text prefix = toText("&b[&fActiveTime&b]&r ");
    private static final int[] constants = {604800, 86400, 3600, 60, 1};
    static final String[] formats = {"#w", "#d", "#h", "#m", "#s", ""};
    private static final Pattern timespan = Pattern.compile("(?:([0-9]+)w)?(?:([0-9]+)d)?(?:([0-9]+)h)?(?:([0-9]+)m)?(?:([0-9])+s)?");

    public static Text toText(String msg) {
        return TextSerializers.FORMATTING_CODE.deserialize(msg);
    }

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
                .name(name)
                .execute(consumer)
                .interval(interval, TimeUnit.MILLISECONDS)
                .submit(ActiveTime.getInstance());
    }

    public static <T> void addTimes(List<Text> texts, String category, Collection<Map.Entry<T, TimeHolder>> times, Function<T, String> function) {
        List<Map.Entry<T, TimeHolder>> filtered = times.stream().filter(e -> e.getValue().getActiveTime() != 0 || e.getValue().getAfkTime() != 0).collect(Collectors.toList());
        texts.add(Util.toText(category + "(" + filtered.size() + ")"));
        filtered.forEach(e -> texts.add(Util.toText(" - &7" + function.apply(e.getKey()) + "&f: &b" + Util.printTime(e.getValue()))));
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

    public static int parseTime(String timeStr) {
        try {
            return Integer.parseInt(timeStr);
        } catch (NumberFormatException ignored) {
            int time = 0;
            Matcher matcher = timespan.matcher(timeStr);
            if (matcher.matches()) {
                for (int i = 1; i <= 5; i++) {
                    time += matcher.group(i) != null ? Integer.parseInt(matcher.group(i)) * constants[i - 1] : 0;
                }
            }
            return time;
        }
    }

    public static String printTime(int time) {
        if (time > 0) {
            List<String> times = Lists.newArrayList();
            for (int i = 0; i < 5; i++) {
                int num = time / constants[i];
                if (num > 0 && !formats[i].isEmpty()) {
                    times.add(formats[i].replace("#", String.valueOf(num)).replace("<s>", num == 1 ? "" : "s"));
                    time -= num * constants[i];
                }
            }
            if (!times.isEmpty()) {
                return String.join(formats[5], times);
            }
        }
        return formats[4].contains("#") ? formats[4].replace("#", String.valueOf(time)).replace("<s>", "s") : time + "s";
    }

    public static String printTime(TimeHolder time) {
        return "&b" + printTime(time.getActiveTime()) + (ActiveTime.isNucleusEnabled() && time.getActiveTime() + time.getAfkTime() != 0 ? " &f| &b" + printTime(time.getAfkTime()) + " &f- &7" + new DecimalFormat("##.##%").format((double) time.getActiveTime() / (time.getActiveTime() + time.getAfkTime())) : "");
    }

    public static String printDate(LocalDate date) {
        return date.getMonthValue() + "-" + String.format("%02d", date.getDayOfMonth());
    }

    public static Stream<Player> getLoggedPlayers() {
        Stream<Player> stream = Sponge.getServer().getOnlinePlayers().stream().filter(p -> p.hasPermission("activetime.log.base"));
        if (!Config.worlds.isEmpty()) {
            stream = stream.filter(p -> Config.worlds.contains(p.getWorld().getName().toLowerCase()));
        }
        if (!Config.gamemodes.isEmpty()) {
            stream = stream.filter(p -> Config.gamemodes.contains(p.get(Keys.GAME_MODE).orElse(GameModes.NOT_SET).getId().toLowerCase()));
        }
        return stream;
    }

    public static void startNameTask(Player player) {
        createTask("ActiveTime UpdateUsername Task (" + player.getUniqueId() + ")", task -> {
            String name = Storage.getUsername(player.getUniqueId());
            if (!player.getName().equals(name)) {
                Storage.setUsername(player.getUniqueId(), player.getName());
                Storage.save();
            }
        }, 0, true);
    }

    public static void startUpdateTask() {
        Storage.updateTask = ActiveTime.isNucleusEnabled() ? createTask("ActiveTime UpdateTimes Task (Nucleus)", task -> {
            Map<Player, Boolean> players = getLoggedPlayers().collect(Collectors.toMap(p -> p, p -> !NucleusIntegration.SERVICE.isAFK(p)));
            createTask("ActiveTime UpdateTimes Async Processor (Nucleus)", t -> players.forEach((p, a) -> addCachedTime(p.getUniqueId(), Config.updateInt, a)), 0, true);
        }, Config.updateInt * 1000 - 1, false) : createTask("ActiveTime UpdateTimes Task", task -> {
            List<Player> players = getLoggedPlayers().collect(Collectors.toList());
            createTask("ActiveTime UpdateTimes Async Processor", t -> players.forEach(p -> addCachedTime(p.getUniqueId(), Config.updateInt, true)), 0, true);
        }, Config.updateInt * 1000 - 1, false);
    }

    public static void addCachedTime(UUID uuid, int time, boolean active) {
        Storage.times.computeIfAbsent(uuid, u -> new TimeHolder()).add(time, active);
    }

    public static void startSaveTask() {
        Storage.saveTask = createTask("ActiveTime SaveTimes Task", task -> {
            ImmutableMap<UUID, TimeHolder> times = ImmutableMap.copyOf(Storage.times);
            Storage.times.clear();
            createTask("ActiveTime SaveTimes Async Processor", t -> {
                times.forEach(Util::saveTime);
                Storage.save();
                Storage.buildLeaderboard();
            }, 0, true);
        }, Config.saveInt * 1000 - 1, false);
    }

    public static void saveTime(UUID uuid, TimeHolder time) {
        Storage.setTotalTime(uuid, Storage.getTotalTime(uuid).add(time));
        Storage.setDailyTime(uuid, Storage.getDailyTime(uuid).add(time));
    }

    public static void startMilestoneTask() {
        Storage.milestoneTask = createTask("ActiveTime CheckMilestones Task", task -> {
            ImmutableList<Player> players = ImmutableList.copyOf(Sponge.getServer().getOnlinePlayers());
            createTask("ActiveTime CheckMilestones Async Processor", t -> {
                players.forEach(Util::checkMilestones);
                Storage.save();
            }, 0, true);
        }, Config.milestoneInt * 1000 - 1, false);
    }

    public static void checkMilestones(Player player) {
        int activetime = Storage.getTotalTime(player.getUniqueId()).getActiveTime();
        Storage.milestones.values().stream()
                .filter(m -> player.hasPermission("activetime.milestones." + m.getName() + ".base"))
                .forEach(m -> m.process(player, activetime));
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