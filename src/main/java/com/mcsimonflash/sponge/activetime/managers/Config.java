package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Config {
    private static ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
            .setPath(ActiveTime.getPlugin().getDefaultConfig()).build();
    private static CommentedConfigurationNode rootNode;

    private static void loadConfig() {
        try {
            rootNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            ActiveTime.getPlugin().getLogger().error("Config could not be loaded!");
        }
    }
    private static void saveConfig() {
        try {
            loader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
            ActiveTime.getPlugin().getLogger().error("Config could not be saved!");
        }
    }

    public static void readConfig() {
        if (Files.notExists(ActiveTime.getPlugin().getDefaultConfig())) {
            try {
                Sponge.getAssetManager().getAsset(ActiveTime.getPlugin(), "defaultConfig.conf").get().copyToFile(ActiveTime.getPlugin().getDefaultConfig());
                ActiveTime.getPlugin().getLogger().warn("Default config loaded into activetime.conf!");
            } catch (IOException e) {
                e.printStackTrace();
                ActiveTime.getPlugin().getLogger().error("Unable to load defaultConfig.conf! Config must be manually loaded.");
            }
        }
        loadConfig();
        if (LogTime.autoSaveTask != null) {
            LogTime.autoSaveTask.cancel();
            LogTime.autoSaveTask = null;
        }
        int autoSave = rootNode.getNode("config", "auto-save").getInt(0);
        if (autoSave > 0) {
            LogTime.autoSaveTask = Task.builder().async()
                    .name("ActiveTime AutoSave")
                    .execute(LogTime::saveTimes)
                    .delay(autoSave, TimeUnit.SECONDS)
                    .interval(autoSave, TimeUnit.SECONDS)
                    .submit(ActiveTime.getPlugin());
        }
        Util.updatePlayerTimes();
    }

    public static void updatePlayer(Player player, int newTime) {
        loadConfig();
        int oldTime = Util.parseTime(getTime(player));
        rootNode.getNode("players", player.getUniqueId().toString(), "name").setValue(player.getName());
        rootNode.getNode("players", player.getUniqueId().toString(), "time").setValue(Util.printTime(oldTime + newTime));
        saveConfig();
    }

    public static String getTime(User user) {
        loadConfig();
        return rootNode.getNode("players", user.getUniqueId().toString(), "time").getString("");
    }

    public static int getDefaultRange() {
        loadConfig();
        return rootNode.getNode("config", "default-range").getInt(5);
    }

    public static int getMaxRange() {
        loadConfig();
        return rootNode.getNode("config", "max-range").getInt(10);
    }

    public static Map<String, Integer> getPlayerTimesMap() {
        loadConfig();
        Map<String, Integer> playerTimesMap = Maps.newHashMap();
        Map<Object, ? extends CommentedConfigurationNode> playerUUIDs = rootNode.getNode("players").getChildrenMap();
        int i = 1;
        for (Map.Entry<Object, ? extends ConfigurationNode> uuid : playerUUIDs.entrySet()) {
            playerTimesMap.put(rootNode.getNode("players", uuid.getKey().toString(), "name").getString("?" + i++), Util.parseTime(rootNode.getNode("players", uuid.getKey().toString(), "time").getString("0")));
        }
        return playerTimesMap;
    }
}
