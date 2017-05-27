package com.mcsimonflash.sponge.activetime.managers;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class NucleusIntegration {

    public static void RegisterMessageToken() {
        try {
            NucleusAPI.getMessageTokenService().register(ActiveTime.getPluginContainer(), (tokenInput, source, variables) -> {
                if (tokenInput.equals("time") && source instanceof Player) {
                    String activeTime = Config.getTime((Player) source);
                    return Optional.of(Text.of(activeTime.isEmpty() ? "No Time Logged" : activeTime));
                }
                return Optional.empty();
            });
            if (!NucleusAPI.getMessageTokenService().registerPrimaryToken("activetime", ActiveTime.getPluginContainer(), "time")) {
                ActiveTime.getPlugin().getLogger().warn("Could not register Nucleus shorthand token {{activetime}}!");
            }
        } catch (PluginAlreadyRegisteredException ignored) {
            ActiveTime.getPlugin().getLogger().error("Attempted duplicate registration ActiveTime Nucleus token");
        }
    }

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
