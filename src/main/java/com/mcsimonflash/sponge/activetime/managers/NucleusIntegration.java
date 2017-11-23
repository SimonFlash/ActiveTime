package com.mcsimonflash.sponge.activetime.managers;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

public class NucleusIntegration {

    private static NucleusAFKService afkService = NucleusAPI.getAFKService().get();

    public static void RegisterMessageToken() {
        try {
            PluginContainer container = Sponge.getPluginManager().getPlugin("activetime").get();
            NucleusAPI.getMessageTokenService().register(container, (tokenInput, source, variables) -> {

                boolean active;
                if (tokenInput.equalsIgnoreCase("activetime")) {
                    active = true;
                } else if (tokenInput.equalsIgnoreCase("afktime")) {
                    active = false;
                } else {
                    return Optional.empty();
                }
                if (source instanceof User) {
                    return Optional.of(Util.toText(Util.printTime(Storage.getTotalTime(((User) source).getUniqueId()).getTime(active))));
                } else {
                    return Optional.of(Util.toText(active ? "∞" : "√-1"));
                }
            });
            NucleusAPI.getMessageTokenService().registerPrimaryToken("activetime", container, "activetime");
            NucleusAPI.getMessageTokenService().registerPrimaryToken("afktime", container, "afktime");
        } catch (PluginAlreadyRegisteredException ignored) {
            ActiveTime.getPlugin().getLogger().error("Attempted duplicate registration ActiveTime Nucleus token");
        }
    }

    public static void updateAFKService() {
        afkService = NucleusAPI.getAFKService().get();
    }

    public static boolean isPlayerAfk(Player player) {
        return afkService.isAFK(player);
    }

}