package com.mcsimonflash.sponge.activetime.managers;

import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;

public class NucleusListeners {

    @Listener
    public void onNucleusAFKEvent$GoingAFK(NucleusAFKEvent.GoingAFK event, @First Player player) {
        if (LogTime.activeTimeMap.containsKey(player)) {
            LogTime.saveTime(player);
            LogTime.activeTimeMap.remove(player);
        }
    }

    @Listener
    public void onNucleusAFKEvent$ReturningFromAFK(NucleusAFKEvent.ReturningFromAFK event, @First Player player) {
        if (player.hasPermission("activetime.log")) {
            LogTime.activeTimeMap.put(player, System.nanoTime());
        }
    }
}
