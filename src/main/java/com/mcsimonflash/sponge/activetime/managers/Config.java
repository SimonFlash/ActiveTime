package com.mcsimonflash.sponge.activetime.managers;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.ConfigWrapper;
import com.mcsimonflash.sponge.activetime.objects.Milestone;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static Path rootDir = ActiveTime.getPlugin().getDirectory().resolve("activetime"), confDir = rootDir.resolve("configuration");
    private static ConfigWrapper core, milestones;

    public static int updateInt, saveInt, milestoneInt, limitInt, defaultPos, maximumPos;

    public static void readConfig() {
        try {
            Files.createDirectories(confDir);
            core = new ConfigWrapper(rootDir.resolve("activetime.core"), true);
            milestones = new ConfigWrapper(confDir.resolve("milestones.conf"), true);
        } catch (IOException e) {
            ActiveTime.getPlugin().getLogger().error("Config loading has halted due to an unexpected error!");
            e.printStackTrace();
            return;
        }
        updateInt = core.getNode().getNode("intervals", "update").getInt(1);
        if (updateInt <= 0) {
            ActiveTime.getPlugin().getLogger().warn("The update interval must be greater than 0! Reverting to 1.");
            updateInt = 1;
        }
        saveInt = core.getNode().getNode("intervals", "save").getInt(30);
        if (saveInt <= 0) {
            ActiveTime.getPlugin().getLogger().warn("The save interval must be greater than 0! Reverting to 30.");
            saveInt = 30;
        }
        if (updateInt > saveInt) {
            ActiveTime.getPlugin().getLogger().warn("The update interval must not be greater than the save interval. Reverting to defaults.");
            updateInt = 1;
            saveInt = 30;
        }
        defaultPos = core.getNode().getNode("leaderboard", "default").getInt(10);
        if (defaultPos <= 0) {
            ActiveTime.getPlugin().getLogger().warn("The default leaderboard position must be greater than 0! Reverting to 10.");
            defaultPos = 10;
        }
        maximumPos = core.getNode().getNode("leaderboard", "maximum").getInt(100);
        if (maximumPos <= 0) {
            ActiveTime.getPlugin().getLogger().warn("The maximum leadership position must be greater than 0! Reverting to 10.");
            maximumPos = 10;
        }
        if (defaultPos > maximumPos) {
            ActiveTime.getPlugin().getLogger().warn("The maximum leaderboard position must not be greater than the default. Reverting to defaults.");
            defaultPos = 10;
            maximumPos = 100;
        }
        Storage.milestones.clear();
        milestoneInt = core.getNode().getNode("intervals", "milestone").getInt(60);
        milestones.getNode().getChildrenMap().values().forEach(Config::loadMilestone);
        if (milestoneInt <= 0 && !Storage.milestones.isEmpty()) {
            ActiveTime.getPlugin().getLogger().warn("Loaded milestones, but the milestone task is not enabled!");
        }
    }

    public static void loadMilestone(CommentedConfigurationNode node) {
        int activetime = node.getNode("activetime").getInt();
        if (activetime <= 0) {
            ActiveTime.getPlugin().getLogger().error("Milestone activetime must be greater than 0. | Milestone:[" + node.getKey() + "]");
            return;
        }
        String command = node.getNode("command").getString("");
        if (command.isEmpty()) {
            ActiveTime.getPlugin().getLogger().error("Milestone command is empty! | Milestone:[" + node.getKey() + "]");
            return;
        }
        Storage.milestones.add(new Milestone((String) node.getKey(), activetime, command));
    }

}