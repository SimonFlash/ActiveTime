package com.mcsimonflash.sponge.activetime.managers;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.ConfigWrapper;
import com.mcsimonflash.sponge.activetime.objects.Milestone;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static Path rootDir = ActiveTime.getPlugin().getDirectory().resolve("activetime");
    private static Path confDir = rootDir.resolve("configuration");
    private static ConfigWrapper core, milestones;

    public static int updateInterval;
    public static int saveInterval;
    public static int milestoneInterval;
    public static int limitInterval;
    public static int defaultPositions;
    public static int maximumPositions;

    private static boolean initializeNodes() {
        try {
            Files.createDirectories(confDir);
            core = new ConfigWrapper(rootDir.resolve("activetime.core"), true);
            milestones = new ConfigWrapper(confDir.resolve("milestones.conf"), true);
            return true;
        } catch (IOException e) {
            ActiveTime.getPlugin().getLogger().error("Config loading has halted due to an unexpected error!");
            e.printStackTrace();
            return false;
        }
    }

    public static void readConfig() {
        if (initializeNodes()) {
            updateInterval = core.getNode().getNode("intervals", "update").getInt(1);
            saveInterval = core.getNode().getNode("intervals", "save").getInt(30);
            milestoneInterval = core.getNode().getNode("intervals", "milestone").getInt(60);
            defaultPositions = core.getNode().getNode("leaderboard", "default").getInt(10);
            maximumPositions = core.getNode().getNode("leaderboard", "maximum").getInt(100);
            Storage.milestones.clear();
            milestones.getNode().getChildrenMap().values().forEach(Config::loadMilestone);
            if (milestoneInterval <= 0 && !Storage.milestones.isEmpty()) {
                ActiveTime.getPlugin().getLogger().warn("Loaded milestones, but the milestone task is not enabled!");
            }
        }
    }

    public static void loadMilestone(CommentedConfigurationNode node) {
        int activetime = node.getNode("activetime").getInt(0);
        if (activetime <= 0) {
            ActiveTime.getPlugin().getLogger().error("activetime must be greater than 0. | Milestone:[" + node.getKey() + "]");
            return;
        }
        String command = node.getNode("command").getString("");
        if (command.isEmpty()) {
            ActiveTime.getPlugin().getLogger().error("command is empty! | Milestone:[" + node.getKey() + "]");
            return;
        }
        Storage.milestones.add(new Milestone((String) node.getKey(), activetime, command));
    }

}