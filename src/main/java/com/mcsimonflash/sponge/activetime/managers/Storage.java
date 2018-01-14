package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.ConfigWrapper;
import com.mcsimonflash.sponge.activetime.objects.Milestone;
import com.mcsimonflash.sponge.activetime.objects.Report;
import com.mcsimonflash.sponge.activetime.objects.TimeWrapper;
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

    private static Path storDir = ActiveTime.getPlugin().getDirectory().resolve("activetime").resolve("storage");
    private static Path logsDir = storDir.resolve("logs");
    private static ConfigWrapper players, current;

    public static List<Text> leaderboard = Lists.newLinkedList();
    public static List<Milestone> milestones = Lists.newArrayList();
    public static Map<UUID, TimeWrapper> times = Maps.newHashMap();
    public static Task updateTask;
    public static Task saveTask;
    public static Task milestoneTask;
    public static Task limitTask;

    private static void initializeNodes() {
        try {
            Files.createDirectories(logsDir);
            players = new ConfigWrapper(storDir.resolve("players.stor"), false);
            syncCurrentDate();
        } catch (IOException e) {
            ActiveTime.getPlugin().getLogger().error("Unable to load storage files! Error:");
            e.printStackTrace();
        }
    }

    public static boolean syncCurrentDate() {
        try {
            current = new ConfigWrapper(logsDir.resolve(LocalDate.now() + ".stor"), false);
            return true;
        } catch (IOException e) {
            ActiveTime.getPlugin().getLogger().error("Unable to initiate daily log file!");
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

    private static TimeWrapper getTimeWrapper(CommentedConfigurationNode node) {
        return new TimeWrapper(node.getNode("activetime").getInt(), node.getNode("afktime").getInt());
    }

    private static void setTimeWrapper(CommentedConfigurationNode node, TimeWrapper time) {
        node.getNode("activetime").setValue(time.getActiveTime());
        node.getNode("afktime").setValue(time.getAfkTime());
    }

    public static TimeWrapper getTotalTime(UUID uuid) {
        return getTimeWrapper(players.getNode(uuid.toString()));
    }

    public static boolean setTotalTime(UUID uuid, TimeWrapper time) {
        setTimeWrapper(players.getNode(uuid.toString()), time);
        return players.save();
    }

    public static TimeWrapper getDailyTime(UUID uuid) {
        return getTimeWrapper(current.getNode(uuid.toString()));
    }

    public static boolean setDailyTime(UUID uuid, TimeWrapper time) {
        setTimeWrapper(current.getNode(uuid.toString()), time);
        return current.save();
    }

    public static Optional<TimeWrapper> getTime(UUID uuid, LocalDate date) {
        Path path = logsDir.resolve(date + ".stor");
        if (Files.exists(path)) {
            try {
                return Optional.of(getTimeWrapper(new ConfigWrapper(path, false).getNode(uuid.toString())));
            } catch (IOException e) {
                ActiveTime.getPlugin().getLogger().error("An error occurred loading the log file for " + date);
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public static boolean hasMilestone(UUID uuid, String milestone) {
        return players.getNode(uuid.toString(), "milestones", milestone).getBoolean(false);
    }

    public static boolean setMilestone(UUID uuid, String milestone, boolean obtained) {
        players.getNode(uuid.toString(), "milestones", milestone).setValue(obtained);
        return players.save();
    }

    public static void buildLeaderboard() {
        AtomicInteger i = new AtomicInteger(1);
        leaderboard = players.getNode().getChildrenMap().values().stream()
                .map(n -> Maps.immutableEntry(n.getNode("username").getString((String) n.getKey()), getTimeWrapper(n)))
                .sorted(Comparator.comparingInt(o -> -o.getValue().getActiveTime()))
                .map(e -> "&b" + i.getAndIncrement() + "&7: &f" + e.getKey() + " &7- " + Util.printTime(e.getValue().getActiveTime()))
                .map(Util::toText)
                .collect(Collectors.toList());
    }

    public static Report getReport(UUID uuid, int days) {
        Report report = new Report();
        report.uuid = uuid;
        report.name = getUsername(uuid);
        report.total = getTotalTime(uuid);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);
        LocalDate week = start.minusDays(start.getDayOfWeek().getValue());
        LocalDate month = start.minusDays(start.getDayOfMonth() - 1);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Path path = logsDir.resolve(date + ".stor");
            if (Files.exists(path)) {
                try {
                    TimeWrapper time = getTimeWrapper(new ConfigWrapper(path, false).getNode(uuid.toString()));
                    report.dailyTimes.put(date, time);
                    report.weeklyTimes.computeIfAbsent((week = date.getDayOfWeek().getValue() == 7 ? date : week), w -> new TimeWrapper()).add(time);
                    report.monthlyTimes.computeIfAbsent((month = date.getDayOfMonth() == 1 ? date : month), m -> new TimeWrapper()).add(time);
                } catch (IOException e) {
                    report.error = true;
                    ActiveTime.getPlugin().getLogger().error("Unable to generate ActiveTime Report for uuid " + uuid + " with log " + path.getFileName() + "!");
                    e.printStackTrace();
                }
            }
        }
        if (!report.dailyTimes.isEmpty()) {
            report.dailyTimes.values().forEach(t -> report.dailyAverage.add(t));
            report.dailyAverage.set(report.dailyAverage.getActiveTime() / report.dailyTimes.size(), report.dailyAverage.getAfkTime() / report.dailyTimes.size());
            report.weeklyTimes.values().forEach(t -> report.weeklyAverage.add(t));
            report.weeklyAverage.set(report.weeklyAverage.getActiveTime() / report.weeklyTimes.size(), report.weeklyAverage.getAfkTime() / report.weeklyTimes.size());
            report.monthlyTimes.values().forEach(t -> report.monthlyAverage.add(t));
            report.monthlyAverage.set(report.monthlyAverage.getActiveTime() / report.monthlyTimes.size(), report.monthlyAverage.getAfkTime() / report.monthlyTimes.size());
        }
        return report;
    }

    public static Report.Daily getDailyReport(LocalDate date) {
        Report.Daily report = new Report.Daily();
        report.date = date;
        Path path = logsDir.resolve(date + ".stor");
        if (Files.exists(path)) {
            try {
                ConfigWrapper config = new ConfigWrapper(path, false);
                config.getNode().getChildrenMap().values().forEach(n -> {
                    TimeWrapper time = getTimeWrapper(n);
                    report.total.add(time);
                    report.userTimes.add(Maps.immutableEntry(players.getNode(n.getKey()).getNode("username").getString((String) n.getKey()), time));
                });
                if (!report.userTimes.isEmpty()) {
                    report.average.set(report.total.getActiveTime() / report.userTimes.size(), report.total.getAfkTime() / report.userTimes.size());
                    report.userTimes.sort(Comparator.comparingInt(o -> -o.getValue().getActiveTime()));
                }
            } catch (IOException e) {
                report.error = true;
                ActiveTime.getPlugin().getLogger().error("Unable to generate ActiveTime Report for date " + date + " with log " + path.getFileName() + "!");
                e.printStackTrace();
            }
        }
        return report;
    }

}