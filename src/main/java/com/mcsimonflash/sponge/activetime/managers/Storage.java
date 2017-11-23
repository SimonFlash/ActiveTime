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

public class Storage {

    private static Path storDir = ActiveTime.getPlugin().getDirectory().resolve("activetime").resolve("storage");
    private static Path logsDir = storDir.resolve("logs");
    private static ConfigWrapper players, current;

    public static LinkedList<Text> leaderboard = Lists.newLinkedList();
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
        return players.getNode(uuid.toString(), "username").getString("???");
    }

    public static boolean setUsername(UUID uuid, String name) {
        players.getNode(uuid.toString(), "username").setValue(name);
        return players.save();
    }

    private static TimeWrapper getTimeWrapper(CommentedConfigurationNode node) {
        return new TimeWrapper(node.getNode("activetime").getInt(0), node.getNode("afktime").getInt(0));
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
        Map<String, TimeWrapper> times = Maps.newHashMap();
        for (CommentedConfigurationNode node : players.getNode().getChildrenMap().values()) {
            times.put(node.getNode("username").getString((String) node.getKey()), new TimeWrapper(node.getNode("activetime").getInt(0), node.getNode("afktime").getInt(0)));
        }
        LinkedList<String> players = Lists.newLinkedList(times.keySet());
        players.sort(Comparator.comparingInt(o -> -times.get(o).getActiveTime()));
        LinkedList<Text> tempLeaderboard = Lists.newLinkedList();
        for (int i = 0; i < players.size(); i++) {
            String player = players.get(i);
            tempLeaderboard.add(Util.toText("&b" + (i + 1) + "&7: &f" + player + " &7- " + Util.printTime(times.get(player))));
        }
        leaderboard = tempLeaderboard;
    }

    public static Report getReport(UUID uuid, int size) {
        Report report = new Report();
        report.name = getUsername(uuid);
        report.uuid = uuid;
        report.total = getTotalTime(uuid);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(size);
        LocalDate week = start.minusDays(start.getDayOfWeek().getValue());
        LocalDate month = start.minusDays(start.getDayOfMonth() - 1);
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Path path = logsDir.resolve(date + ".stor");
            TimeWrapper time = new TimeWrapper(0, 0);
            if (Files.exists(path)) {
                try {
                    ConfigWrapper log = new ConfigWrapper(path, false);
                    time.add(getTimeWrapper(log.getNode(uuid.toString())));
                } catch (IOException e) {
                    report.error = true;
                    ActiveTime.getPlugin().getLogger().error("Unable to generate ActiveTime Report for uuid " + uuid + " with log " + path.getFileName() + "!");
                    e.printStackTrace();
                }
            }
            report.dailyTimes.put(date, time);
            week = date.getDayOfWeek().getValue() == 7 ? date : week;
            report.weeklyTimes.putIfAbsent(week, new TimeWrapper(0, 0));
            report.weeklyTimes.get(week).add(time);
            month = date.getDayOfMonth() == 1 ? date : month;
            report.monthlyTimes.putIfAbsent(month, new TimeWrapper(0, 0));
            report.monthlyTimes.get(month).add(time);
        }
        report.dailyTimes.values().forEach(t -> report.dailyAverage.add(t));
        report.dailyAverage.set(report.dailyAverage.getActiveTime() / report.dailyTimes.size(), report.dailyAverage.getAfkTime() / report.dailyTimes.size());
        report.weeklyTimes.values().forEach(t -> report.weeklyAverage.add(t));
        report.weeklyAverage.set(report.weeklyAverage.getActiveTime() / report.weeklyTimes.size(), report.weeklyAverage.getAfkTime() / report.weeklyTimes.size());
        report.monthlyTimes.values().forEach(t -> report.monthlyAverage.add(t));
        report.monthlyAverage.set(report.monthlyAverage.getActiveTime() / report.monthlyTimes.size(), report.monthlyAverage.getAfkTime() / report.monthlyTimes.size());
        return report;
    }

}