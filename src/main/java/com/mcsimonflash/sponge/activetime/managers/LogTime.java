package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.List;
import java.util.Map;

public class LogTime {

    public static Task autoSaveTask;
    public static List<String> playerTimes = Lists.newLinkedList();
    public static Map<Player, Long> activeTimeMap = Maps.newHashMap();

    public static void saveTimes() {
        for (Player player : activeTimeMap.keySet()) {
            if (Sponge.getServer().getPlayer(player.getUniqueId()).isPresent()) {
                saveTime(player);
            } else {
                ActiveTime.getPlugin().getLogger().warn("Attempted to log " + player.getName() + ", but player is not online!");
                activeTimeMap.remove(player);
            }
        }
        Util.updatePlayerTimes();
    }

    public static void saveTime(Player player) {
        int time = (int) ((System.nanoTime() - activeTimeMap.get(player))/1e9);
        activeTimeMap.put(player, System.nanoTime());
        Config.updatePlayer(player, time);
    }
}
