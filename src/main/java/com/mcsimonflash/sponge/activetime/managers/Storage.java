package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.ConfigWrapper;
import com.mcsimonflash.sponge.activetime.objects.Milestone;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Storage {

    private static Path storDir = ActiveTime.getPlugin().getDirectory().resolve("activetime").resolve("storage");
    private static Path logsDir = storDir.resolve("logs");
    private static ConfigWrapper players, current;

    static List<Milestone> milestones = Lists.newArrayList();
    static Map<UUID, Integer> activetimes = Maps.newHashMap();
    static Map<UUID, Integer> afktimes = Maps.newHashMap();
    static Task updateTask;
    static Task saveTask;
    static Task milestoneTask;

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
            current = new ConfigWrapper(logsDir.resolve(Util.getFileName(Calendar.getInstance()) + ".stor"), false);
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
        return players.node().getNode(uuid.toString(), "username").getString("???");
    }

    public static boolean setUsername(UUID uuid, String name) {
        players.node().getNode(uuid.toString(), "username").setValue(name);
        return players.save();
    }

    public static int getTotalTime(UUID uuid, boolean active) {
        return players.node().getNode(uuid.toString(), active ? "activetime" : "afktime").getInt(0);
    }

    public static boolean setTotalTime(UUID uuid, int time, boolean active) {
        players.node().getNode(uuid.toString(), active ? "activetime" : "afktime").setValue(time);
        return players.save();
    }

    public static int getDailyTime(UUID uuid, boolean active) {
        return current.node().getNode(uuid.toString(), active ? "activetime" : "afktime").getInt(0);
    }

    public static boolean setDailyTime(UUID uuid, int time, boolean active) {
        current.node().getNode(uuid.toString(), active ? "activetime" : "afktime").setValue(time);
        return current.save();
    }

    public static boolean hasMilestone(UUID uuid, String milestone) {
        return players.node().getNode(uuid.toString(), "milestones", milestone).getBoolean(false);
    }

    public static boolean setMilestone(UUID uuid, String milestone, boolean obtained) {
        players.node().getNode(uuid.toString(), "milestones", milestone).setValue(obtained);
        return players.save();
    }
}