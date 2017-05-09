package com.mcsimonflash.sponge.activetime;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mcsimonflash.sponge.activetime.commands.Base;
import com.mcsimonflash.sponge.activetime.commands.Check;
import com.mcsimonflash.sponge.activetime.commands.Top;
import com.mcsimonflash.sponge.activetime.managers.Config;
import com.mcsimonflash.sponge.activetime.managers.NucleusListeners;
import com.mcsimonflash.sponge.activetime.managers.LogTime;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

@Plugin(id = "activetime", name = "ActiveTime", version = "mc1.10.2-v1.0.2", description = "Simple Playertime Tracker", authors = "Simon_Flash")
public class ActiveTime {

    private static ActiveTime plugin;
    public static ActiveTime getPlugin() {
        return plugin;
    }

    private static URL wiki;
    public static URL getWiki() {
        return wiki;
    }

    private static URL discord;
    public static URL getDiscord() {
        return discord;
    }

    @Inject
    private Logger logger;
    public Logger getLogger() {
        return logger;
    }

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    public Path getDefaultConfig() {
        return defaultConfig;
    }

    @Listener
    public void onInitilization(GameInitializationEvent event) {
        plugin = this;
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        logger.info("|     ActiveTime -- Version 1.0.2     |");
        logger.info("|      Developed By: Simon_Flash      |");
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        Config.readConfig();
        try {
            wiki = new URL("https://github.com/SimonFlash/ActiveTime/wiki");
        } catch (MalformedURLException ignored) {
            getLogger().error("Unable to locate ActiveTime Wiki!");
        }
        try {
            discord = new URL("https://discordapp.com/invite/4wayq37");
        } catch (MalformedURLException ignored) {
            getLogger().error("Unable to locate Support Discord!");
        }

        CommandSpec Check = CommandSpec.builder()
                .executor(new Check())
                .description(Text.of("Shows a player's ActiveTime status"))
                .permission("activetime.check.base")
                .arguments(
                        GenericArguments.optional(GenericArguments.user(Text.of("user"))))
                .build();
        CommandSpec Top = CommandSpec.builder()
                .executor(new Top())
                .description(Text.of("Displays the most active players"))
                .permission("activetime.top.base")
                .arguments(
                        GenericArguments.optional(GenericArguments.integer(Text.of("range"))))
                .build();
        CommandSpec ActiveTime = CommandSpec.builder()
                .executor(new Base())
                .description(Text.of("Opens in-game documentation"))
                .permission("activetime.base")
                .child(Check, "Check")
                .child(Top, "Top")
                .build();
        Sponge.getCommandManager().register(plugin, ActiveTime, Lists.newArrayList("ActiveTime", "aTime"));
        Sponge.getCommandManager().register(plugin, Check, Lists.newArrayList("PlayTime"));
    }

    @Listener
    public void onPostInitialization(GamePostInitializationEvent event) {
        if (Sponge.getPluginManager().isLoaded("nucleus")) {
            Sponge.getEventManager().registerListeners(plugin, new NucleusListeners());
        } else {
            logger.warn("NucleusListeners could not be found! Disabling Nucleus AFK listeners");
        }
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        Config.readConfig();
    }


    @Listener
    public void onClientConnectionEvent$Join(ClientConnectionEvent.Join event, @First Player player) {
        if (player.hasPermission("activetime.log")) {
            LogTime.activeTimeMap.put(player, System.nanoTime());
        }
    }

    @Listener
    public void onClientConnectionEvent$Disconnect(ClientConnectionEvent.Disconnect event, @First Player player) {
        if (LogTime.activeTimeMap.containsKey(player)) {
            LogTime.saveTime(player);
            LogTime.activeTimeMap.remove(player);
        }
    }
}
