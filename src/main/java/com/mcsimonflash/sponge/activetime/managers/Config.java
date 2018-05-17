package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.ConfigHolder;
import com.mcsimonflash.sponge.activetime.objects.Milestone;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Config {

    private static Path root = ActiveTime.getInstance().getDirectory(), configuration = root.resolve("configuration");
    private static ConfigHolder core, milestones;

    public static int updateInt, saveInt, milestoneInt, limitInt, defaultPos, maximumPos, maximumRep;
    public static ImmutableList<String> gameModes, worlds;

    public static void readConfig() {
        try {
            Files.createDirectories(configuration);
            core = new ConfigHolder(root.resolve("activetime.core"), true);
            milestones = new ConfigHolder(configuration.resolve("milestones.conf"), true);
        } catch (IOException e) {
            ActiveTime.getLogger().error("Config loading has halted due to an unexpected error:");
            e.printStackTrace();
            return;
        }
        updateInt = core.getNode("intervals", "update").getInt(1);
        if (updateInt <= 0) {
            ActiveTime.getLogger().warn("The update interval must be greater than 0. Reverting to 1.");
            updateInt = 1;
        }
        saveInt = core.getNode("intervals", "save").getInt(30);
        if (saveInt <= 0) {
            ActiveTime.getLogger().warn("The save interval must be greater than 0. Reverting to 30.");
            saveInt = 30;
        }
        if (updateInt > saveInt) {
            ActiveTime.getLogger().warn("The update interval must not be greater than the save interval. Reverting to defaults.");
            updateInt = 1;
            saveInt = 30;
        }
        defaultPos = core.getNode("leaderboard", "default").getInt(10);
        if (defaultPos <= 0) {
            ActiveTime.getLogger().warn("The default leaderboard position must be greater than 0. Reverting to 10.");
            defaultPos = 10;
        }
        maximumPos = core.getNode("leaderboard", "maximum").getInt(100);
        if (maximumPos <= 0) {
            ActiveTime.getLogger().warn("The maximum leadership position must be greater than 0. Reverting to 10.");
            maximumPos = 10;
        }
        if (defaultPos > maximumPos) {
            ActiveTime.getLogger().warn("The maximum leaderboard position must not be greater than the default. Reverting to defaults.");
            defaultPos = 10;
            maximumPos = 100;
        }
        maximumRep = core.getNode("report", "maximum").getInt(100);
        if (maximumRep <= 0) {
            ActiveTime.getLogger().error("The maximum report length must be greater than 0. Reverting to 100.");
            maximumRep = 100;
        } else if (maximumRep > 365) {
            ActiveTime.getLogger().warn("The maximum report length is over one year. Large reports may result in performance issues.");
        }
        gameModes = loadFilter("gamemodes");
        worlds = loadFilter("worlds");
        Storage.milestones.clear();
        limitInt = core.getNode("intervals", "limit").getInt(-1);
        milestoneInt = core.getNode("intervals", "milestone").getInt(300);
        int i = 0;
        for (CommentedConfigurationNode node : milestones.getNode().getChildrenMap().values()) {
            int activetime = node.getNode("activetime").getInt();
            if (activetime > 0) {
                try {
                    List<String> commands = node.getNode("commands").getList(TypeToken.of(String.class), Lists::newArrayList);
                    if (!node.getNode("command").isVirtual()) {
                        String command = node.getNode("command").getString("");
                        commands.add(command);
                        node.getNode("command").setValue(null);
                        node.getNode("commands").setValue(commands);
                        i++;
                    }
                    if (!commands.isEmpty()) {
                        Milestone milestone = new Milestone((String) node.getKey(), activetime, node.getNode("repeatable").getBoolean(), commands);
                        Storage.milestones.put(milestone.getName().toLowerCase(), milestone);
                    } else {
                        ActiveTime.getLogger().warn("Empty commands list. | Milestone:[" + node.getKey() + "]");
                    }
                } catch (ObjectMappingException e) {
                    ActiveTime.getLogger().error("Unable to load commands list. | Milestone:[" + node.getKey() + "]");
                    e.printStackTrace();
                }
            } else {
                ActiveTime.getLogger().error("Milestone activetime must be greater than 0. | Milestone:[" + node.getKey() + "]");
            }
        }
        if (i != 0) {
            ActiveTime.getLogger().warn("Updated " + i + " legacy milestone" + (i == 1 ? "" : "s") + " in the milestones.conf file.");
            milestones.save();
        }
        if (Storage.milestones.isEmpty()) {
            ActiveTime.getLogger().warn("No mileston");
            milestoneInt = -1;
        } else if (milestoneInt <= 0) {
            ActiveTime.getLogger().warn("Loaded milestones, but the milestone task was disabled.");
        }
    }

    public static ImmutableList<String> loadFilter(String filter) {
        try {
            return ImmutableList.copyOf(core.getNode("filters", filter).getList(TypeToken.of(String.class), Lists::newArrayList));
        } catch (ObjectMappingException e) {
            ActiveTime.getLogger().error("Unable to load " + filter + " list.");
            e.printStackTrace();
            return ImmutableList.of();
        }
    }

}