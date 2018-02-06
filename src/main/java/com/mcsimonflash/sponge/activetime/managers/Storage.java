package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.*;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Storage {

    private static Path storDir = ActiveTime.getInstance().getDirectory().resolve("storage");
    private static Path logsDir = storDir.resolve("logs");
    private static ConfigHolder players, current;

    public static List<Text> leaderboard = Lists.newLinkedList();
    public static Map<String, Milestone> milestones = Maps.newHashMap();
    public static Map<UUID, TimeHolder> times = Maps.newHashMap();
    public static Task updateTask, saveTask, milestoneTask, limitTask;

    private static void initializeNodes() {
        try {
            Files.createDirectories(logsDir);
            players = new ConfigHolder(storDir.resolve("players.stor"), false);
            int i = 0;
            for (CommentedConfigurationNode node : players.getNode().getChildrenMap().values()) {
                for (CommentedConfigurationNode milestone : node.getNode("milestones").getChildrenMap().values()) {
                    if (milestone.getValue() instanceof Boolean) {
                        if (milestone.getBoolean(false)) {
                            milestone.setValue(Optional.ofNullable(milestones.get(milestone.getKey())).map(Milestone::getActiveTime).orElse(1));
                        } else {
                            milestone.setValue(null);
                        }
                        i++;
                    }
                }
            }
            if (i != 0) {
                ActiveTime.getLogger().warn("Updated " + i + " legacy milestone" + (i == 1 ? "" : "s") + " in the players.stor file.");
                players.save();
            }
            syncCurrentDate();
        } catch (IOException e) {
            ActiveTime.getLogger().error("Unable to load storage files! Error:");
            e.printStackTrace();
        }
    }

    public static boolean syncCurrentDate() {
        try {
            current = new ConfigHolder(logsDir.resolve(LocalDate.now() + ".stor"), false);
            return true;
        } catch (IOException e) {
            ActiveTime.getLogger().error("Unable to initiate daily log file!");
            return false;
        }
    }

    public static void readStorage() {
        initializeNodes();
    }

    public static String getUsername(UUID uuid) {
        return players.getNode(uuid.toString(), "username").getString(uuid.toString());
    }

    public static boolean setUsername(UUID uuid, String name) {
        players.getNode(uuid.toString(), "username").setValue(name);
        return players.save();
    }

    private static TimeHolder getTimeHolder(CommentedConfigurationNode node) {
        return new TimeHolder(node.getNode("activetime").getInt(), node.getNode("afktime").getInt());
    }

    private static void setTimeWrapper(CommentedConfigurationNode node, TimeHolder time) {
        node.getNode("activetime").setValue(time.getActiveTime());
        node.getNode("afktime").setValue(time.getAfkTime());
    }

    public static TimeHolder getTotalTime(UUID uuid) {
        return getTimeHolder(players.getNode(uuid.toString()));
    }

    public static boolean setTotalTime(UUID uuid, TimeHolder time) {
        setTimeWrapper(players.getNode(uuid.toString()), time);
        return players.save();
    }

    public static TimeHolder getDailyTime(UUID uuid) {
        return getTimeHolder(current.getNode(uuid.toString()));
    }

    public static boolean setDailyTime(UUID uuid, TimeHolder time) {
        setTimeWrapper(current.getNode(uuid.toString()), time);
        return current.save();
    }

    public static Optional<TimeHolder> getTime(UUID uuid, LocalDate date) {
        Path path = logsDir.resolve(date + ".stor");
        if (Files.exists(path)) {
            try {
                return Optional.of(getTimeHolder(new ConfigHolder(path, false).getNode(uuid.toString())));
            } catch (IOException e) {
                ActiveTime.getLogger().error("An error occurred loading the log file for " + date + ".");
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public static int getMilestoneTime(UUID uuid, Milestone milestone) {
        return players.getNode(uuid.toString(), "milestones", milestone.getName()).getInt(0);
    }

    public static boolean setMilestoneTime(UUID uuid, Milestone milestone, int time) {
        players.getNode(uuid.toString(), "milestones", milestone.getName()).setValue(time);
        return players.save();
    }

    public static void buildLeaderboard() {
        AtomicInteger i = new AtomicInteger(1);
        leaderboard = players.getNode().getChildrenMap().values().stream()
                .map(n -> Maps.immutableEntry(n.getNode("username").getString((String) n.getKey()), getTimeHolder(n)))
                .sorted(Comparator.comparingInt(o -> -o.getValue().getActiveTime()))
                .map(e -> "&b" + i.getAndIncrement() + "&7: &f" + e.getKey() + " &7- " + Util.printTime(e.getValue().getActiveTime()))
                .map(Util::toText)
                .collect(Collectors.toList());
    }

    public static void generateServerReport(ServerReport report) {
        LocalDate week = report.from.minusDays(report.from.getDayOfWeek().getValue()), month = report.from.withDayOfMonth(1);
        for (LocalDate date = report.to; !date.isBefore(report.from); date = date.minusDays(1)) {
            week = date.getDayOfWeek().getValue() == 7 ? date : week;
            month = date.getDayOfMonth() == 1 ? date : month;
            Path path = logsDir.resolve(date + ".stor");
            if (Files.exists(path)) {
                try {
                    for (CommentedConfigurationNode node : new ConfigHolder(path, false).getNode().getChildrenMap().values()) {
                        UUID uuid = UUID.fromString((String) node.getKey());
                        UserReport user = report.userReports.computeIfAbsent(uuid, u -> new UserReport(u, report.from, report.to));
                        TimeHolder time = getTimeHolder(node);
                        user.dailyTimes.put(date, time);
                        user.weeklyTimes.computeIfAbsent(week, w -> new TimeHolder()).add(time);
                        user.monthlyTimes.computeIfAbsent(month, m -> new TimeHolder()).add(time);
                    }
                } catch (IOException e) {
                    ActiveTime.getLogger().error("An error occurred loading the log file for " + date + ".");
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    ActiveTime.getLogger().error("The log file for " + date + " is malformed.");
                }
            }
        }
        if (!report.userReports.isEmpty()) {
            report.userReports.values().forEach(u -> {
                u.monthlyTimes.values().forEach(u.total::add);
                report.total.add(u.total);
                if (!u.dailyTimes.isEmpty()) {
                    u.dailyAverage.set(u.total.getActiveTime() / u.dailyTimes.size(), u.total.getAfkTime() / u.dailyTimes.size());
                    u.weeklyAverage.set(u.total.getActiveTime() / u.weeklyTimes.size(), u.total.getAfkTime() / u.weeklyTimes.size());
                    u.monthlyAverage.set(u.total.getActiveTime() / u.monthlyTimes.size(), u.total.getAfkTime() / u.monthlyTimes.size());
                    report.dailyAverage.add(u.dailyAverage);
                    report.weeklyAverage.add(u.weeklyAverage);
                    report.monthlyAverage.add(u.monthlyAverage);
                }
            });
        }
    }

    public static void generateUserReport(UserReport report) {
        LocalDate week = report.from.minusDays(report.from.getDayOfWeek().getValue()), month = report.from.withDayOfMonth(1);
        for (LocalDate date = report.to; !date.isBefore(report.from); date = date.minusDays(1)) {
            TimeHolder time = getTime(report.uuid, date).orElseGet(TimeHolder::new);
            report.dailyTimes.put(date, time);
            report.weeklyTimes.computeIfAbsent((week = date.getDayOfWeek().getValue() == 7 ? date : week), w -> new TimeHolder()).add(time);
            report.monthlyTimes.computeIfAbsent((month = date.getDayOfMonth() == 1 ? date : month), m -> new TimeHolder()).add(time);
        }
        report.monthlyTimes.values().forEach(report.total::add);
        if (!report.dailyTimes.isEmpty()) {
            report.dailyAverage.set(report.total.getActiveTime() / report.dailyTimes.size(), report.total.getAfkTime() / report.dailyTimes.size());
            report.weeklyAverage.set(report.total.getActiveTime() / report.weeklyTimes.size(), report.total.getAfkTime() / report.weeklyTimes.size());
            report.monthlyAverage.set(report.total.getActiveTime() / report.monthlyTimes.size(), report.total.getAfkTime() / report.monthlyTimes.size());
        }
    }

}