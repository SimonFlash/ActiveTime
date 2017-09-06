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
    public static Map<UUID, Integer> activetimes = Maps.newHashMap();
    public static Map<UUID, Integer> afktimes = Maps.newHashMap();
    public static Task updateTask;
    public static Task saveTask;
    public static Task milestoneTask;
    public static Task limitTask;

    private static boolean initializeNodes() {
        try {
            Files.createDirectories(logsDir);
            players = new ConfigWrapper(storDir.resolve("players.stor"), false);
            return syncCurrentDate();
        } catch (IOException e) {
            ActiveTime.getPlugin().getLogger().error("Unable to load storage files! Error:");
            e.printStackTrace();
            return false;
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
        return players.getNode().getNode(uuid.toString(), "username").getString("???");
    }

    public static boolean setUsername(UUID uuid, String name) {
        players.getNode().getNode(uuid.toString(), "username").setValue(name);
        return players.save();
    }

    public static int getTotalTime(UUID uuid, boolean active) {
        return players.getNode().getNode(uuid.toString(), active ? "activetime" : "afktime").getInt(0);
    }

    public static boolean setTotalTime(UUID uuid, int time, boolean active) {
        players.getNode().getNode(uuid.toString(), active ? "activetime" : "afktime").setValue(time);
        return players.save();
    }

    public static int getDailyTime(UUID uuid, boolean active) {
        return current.getNode().getNode(uuid.toString(), active ? "activetime" : "afktime").getInt(0);
    }

    public static boolean setDailyTime(UUID uuid, int time, boolean active) {
        current.getNode().getNode(uuid.toString(), active ? "activetime" : "afktime").setValue(time);
        return current.save();
    }

    public static boolean hasMilestone(UUID uuid, String milestone) {
        return players.getNode().getNode(uuid.toString(), "milestones", milestone).getBoolean(false);
    }

    public static boolean setMilestone(UUID uuid, String milestone, boolean obtained) {
        players.getNode().getNode(uuid.toString(), "milestones", milestone).setValue(obtained);
        return players.save();
    }

    public static void buildLeaderboard() {
        Map<String, TimeWrapper> times = Maps.newHashMap();
        for (CommentedConfigurationNode node : players.getNode().getChildrenMap().values()) {
            times.put((String) node.getKey(), new TimeWrapper(node.getNode("activetime").getInt(0), node.getNode("afktime").getInt(0)));
        }
        LinkedList<String> players = Lists.newLinkedList(times.keySet());
        players.sort(Comparator.comparingInt(o -> times.get(o).getActivetime()));
        LinkedList<Text> tempLeaderboard = Lists.newLinkedList();
        for (int i = 0; i < players.size(); i++) {
            String player = players.get(i);
            tempLeaderboard.add(Util.toText("&9" + (i + 1) + ": &f" + player + " &9» &b" + Util.printTime(times.get(player))));
        }
        leaderboard = tempLeaderboard;
    }

    public static Report buildReport(UUID uuid, int size) {
        Report report = new Report();
        report.name = getUsername(uuid);
        report.uuid = uuid;
        report.total = new TimeWrapper(getTotalTime(uuid, true), getTotalTime(uuid, false));
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(size);
        LocalDate week = start.minusDays(start.getDayOfWeek().getValue());
        LocalDate month = start.minusDays(start.getDayOfMonth() - 1);
        for (LocalDate date = start; !date.isAfter(start); date = date.plusDays(1)) {
            Path path = logsDir.resolve(date + ".stor");
            TimeWrapper time = new TimeWrapper();
            if (Files.exists(path)) {
                try {
                    ConfigWrapper log = new ConfigWrapper(path, false);
                    CommentedConfigurationNode node = log.getNode().getNode(uuid.toString());
                    time.add(node.getNode("activetime").getInt(0), node.getNode("afktime").getInt(0));
                } catch (IOException e) {
                    report.error = true;
                    ActiveTime.getPlugin().getLogger().error("Unable to generate ActiveTime Report for uuid " + uuid + " with log " + path.getFileName() + "!");
                    e.printStackTrace();
                }
            }
            report.dailyTimes.put(date, time);
            if (date.getDayOfWeek().getValue() == 7) {
                week = date;
                report.weeklyTimes.put(week, new TimeWrapper());
            }
            report.weeklyTimes.get(week).add(time);
            if (date.getDayOfMonth() == 1) {
                month = date;
                report.monthlyTimes.put(month, new TimeWrapper());
            }
            report.monthlyTimes.get(month).add(time);
        }
        report.dailyTimes.values().forEach(t -> report.dailyAverage.add(t));
        report.dailyAverage.set(report.dailyAverage.getActivetime() / report.dailyTimes.size(), report.dailyAverage.getAfktime() / report.dailyTimes.size());
        report.weeklyTimes.values().forEach(t -> report.weeklyAverage.add(t));
        report.weeklyAverage.set(report.weeklyAverage.getActivetime() / report.weeklyTimes.size(), report.weeklyAverage.getAfktime() / report.weeklyTimes.size());
        report.monthlyTimes.values().forEach(t -> report.monthlyAverage.add(t));
        report.monthlyAverage.set(report.monthlyAverage.getActivetime() / report.monthlyTimes.size(), report.monthlyAverage.getAfktime() / report.monthlyTimes.size());
        return report;
    }

}