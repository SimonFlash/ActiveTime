package com.mcsimonflash.sponge.activetime;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mcsimonflash.sponge.activetime.commands.Base;
import com.mcsimonflash.sponge.activetime.commands.Check;
import com.mcsimonflash.sponge.activetime.managers.Config;
import com.mcsimonflash.sponge.activetime.managers.NucleusIntegration;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

@Plugin(id = "activetime", name = "ActiveTime", version = "1.4.1", dependencies = @Dependency(id = "nucleus", optional = true), authors = "Simon_Flash")
public class ActiveTime {

    private static ActiveTime instance;
    @Inject private PluginContainer container;
    @Inject private Logger logger;
    private URL wiki, discord;
    private boolean nucleusEnabled;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path directory;
    public Path getDirectory() {
        return directory;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        instance = this;
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        logger.info("|     ActiveTime -- Version 1.4.1     |");
        logger.info("|      Developed By: Simon_Flash      |");
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        Util.initialize();
        try {
            wiki = new URL("https://github.com/SimonFlash/ActiveTime/wiki");
            discord = new URL("https://discordapp.com/invite/4wayq37");
        } catch (MalformedURLException ignored) {
            logger.error("Unable to locate ActiveTime Wiki / Support Discord!");
        }
        Sponge.getCommandManager().register(instance, Base.SPEC, Lists.newArrayList("activetime", "atime"));
        Sponge.getCommandManager().register(instance, Check.SPEC, Lists.newArrayList("ontime", "playtime"));
    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        if (Sponge.getPluginManager().isLoaded("nucleus")) {
            if (NucleusIntegration.SERVICE != null) {
                nucleusEnabled = true;
                NucleusIntegration.registerMessageToken();
            } else {
                ActiveTime.getLogger().error("Unable to obtain the Nucleus AFK Service - please ensure the AFK module is enabled. AFK times will not be logged.");
            }
        } else {
            ActiveTime.getLogger().warn("Nucleus is not installed on this server. AFK times will not be logged.");
        }
        Util.startTasks();
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        Util.initialize();
        Util.startTasks();
    }

    @Listener
    public void onClientConnect(ClientConnectionEvent.Join event, @Root Player player) {
        Util.startNameTask(player);
        if (Config.limitInt > 0) {
            Util.createTask("ActiveTime CheckPlayerLimit Async Processor", task -> Util.checkLimit(player), 0, false);
        }
    }

    public static ActiveTime getInstance() {
        return instance;
    }
    public static PluginContainer getContainer() {
        return instance.container;
    }
    public static Logger getLogger() {
        return instance.logger;
    }
    public static URL getWiki() {
        return instance.wiki;
    }
    public static URL getDiscord() {
        return instance.discord;
    }
    public static boolean isNucleusEnabled() {
        return instance.nucleusEnabled;
    }

}