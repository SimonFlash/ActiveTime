package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.objects.ConfigHolder;
import com.mcsimonflash.sponge.activetime.objects.Milestone;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {

    private static Path root = ActiveTime.getInstance().getDirectory(), configuration = root.resolve("configuration");
    private static ConfigHolder core, milestones;

    public static int updateInt, saveInt, milestoneInt, limitInt, defaultPos, maximumPos, maximumRep;
    public static ImmutableList<String> gamemodes, worlds;

    public static void readConfig() {
        Storage.milestones.clear();
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
        gamemodes = getStringList(core.getNode("filters", "gamemodes"), "gamemode filters");
        worlds = getStringList(core.getNode("filters", "worlds"), "world filters");
        limitInt = core.getNode("intervals", "limit").getInt(-1);
        milestoneInt = core.getNode("intervals", "milestone").getInt(300);
        Util.formats[0] = core.getNode("formats", "weeks").getString("#w");
        Util.formats[1] = core.getNode("formats", "days").getString("#d");
        Util.formats[2] = core.getNode("formats", "hours").getString("#h");
        Util.formats[3] = core.getNode("formats", "minutes").getString("#m");
        Util.formats[4] = core.getNode("formats", "seconds").getString("#s");
        Util.formats[5] = core.getNode("formats", "separator").getString("");
        milestones.getNode().getChildrenMap().values().forEach(m -> {
            int activetime = m.getNode("activetime").getInt(0);
            if (activetime > 0) {
                ImmutableList<String> commands = getStringList(m.getNode("commands"), "milestone commands");
                if (!commands.isEmpty()) {
                    Milestone milestone = new Milestone((String) m.getKey(), activetime, m.getNode("repeatable").getBoolean(false), commands);
                    Storage.milestones.put(milestone.getName().toLowerCase(), milestone);
                } else {
                    ActiveTime.getLogger().warn("Empty commands list. | Milestone:[" + m.getKey() + "]");
                }
            } else {
                ActiveTime.getLogger().error("Milestone activetime must be greater than 0. | Milestone:[" + m.getKey() + "]");
            }
        });
        if (Storage.milestones.isEmpty()) {
            ActiveTime.getLogger().warn("No milestones loaded, disabling the milestone task.");
            milestoneInt = -1;
        } else if (milestoneInt <= 0) {
            ActiveTime.getLogger().warn("Loaded milestones, but the milestone task was disabled.");
        }
    }

    private static ImmutableList<String> getStringList(ConfigurationNode node, String type) {
        try {
            return ImmutableList.copyOf(node.getList(TypeToken.of(String.class), Lists::newArrayList));
        } catch (ObjectMappingException e) {
            ActiveTime.getLogger().error("Unable to load " + type + " list.");
            return ImmutableList.of();
        }
    }

}