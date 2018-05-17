package com.mcsimonflash.sponge.activetime.managers;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

public class NucleusIntegration {

    public static final NucleusAFKService SERVICE = NucleusAPI.getAFKService().orElse(null);

    public static void registerMessageToken() {
        try {
            NucleusAPI.getMessageTokenService().register(ActiveTime.getContainer(), (tokenInput, source, variables) -> {
                boolean active;
                if (tokenInput.equalsIgnoreCase("activetime")) {
                    active = true;
                } else if (tokenInput.equalsIgnoreCase("afktime")) {
                    active = false;
                } else {
                    return Optional.empty();
                }
                return Optional.of(Util.toText(source instanceof User ? Util.printTime(Storage.getTotalTime(((User) source).getUniqueId()).getTime(active)) : active ? "∞" : "√-1"));
            });
            NucleusAPI.getMessageTokenService().registerPrimaryToken("activetime", ActiveTime.getContainer(), "activetime");
            NucleusAPI.getMessageTokenService().registerPrimaryToken("afktime", ActiveTime.getContainer(), "afktime");
        } catch (PluginAlreadyRegisteredException ignored) {
            ActiveTime.getLogger().error("Attempted duplicate registration ActiveTime Nucleus token");
        }
    }

}