package com.mcsimonflash.sponge.activetime;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mcsimonflash.sponge.activetime.commands.Base;
import com.mcsimonflash.sponge.activetime.commands.Check;
import com.mcsimonflash.sponge.activetime.commands.Report;
import com.mcsimonflash.sponge.activetime.commands.Leaderboard;
import com.mcsimonflash.sponge.activetime.managers.Config;
import com.mcsimonflash.sponge.activetime.managers.NucleusIntegration;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

@Plugin(id = "activetime", name = "ActiveTime", version = "s6.0-v1.2.2", authors = "Simon_Flash")
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

    private static boolean nucleusEnabled;
    public static boolean isNucleusEnabled() {
        return nucleusEnabled;
    }

    @Inject
    private Logger logger;
    public Logger getLogger() {
        return logger;
    }

    @Inject
    @ConfigDir(sharedRoot = true)
    private Path directory;
    public Path getDirectory() {
        return directory;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        plugin = this;
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        logger.info("|     ActiveTime -- Version 1.2.2     |");
        logger.info("|      Developed By: Simon_Flash      |");
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        try {
            wiki = new URL("https://github.com/SimonFlash/ActiveTime/wiki");
            discord = new URL("https://discordapp.com/invite/4wayq37");
        } catch (MalformedURLException ignored) {
            getLogger().error("Unable to locate ActiveTime Wiki / Support Discord!");
        }
        Util.initialize();
        CommandSpec Check = CommandSpec.builder()
                .executor(new Check())
                .description(Text.of("Shows a player's ActiveTime status"))
                .permission("activetime.check.base")
                .arguments(
                        GenericArguments.optional(GenericArguments.user(Text.of("user"))))
                .build();
        CommandSpec Leaderboard = CommandSpec.builder()
                .executor(new Leaderboard())
                .description(Text.of("Displays the leaderboard of active players"))
                .permission("activetime.leaderboard.base")
                .arguments(
                        GenericArguments.optional(GenericArguments.integer(Text.of("positions"))))
                .build();
        CommandSpec Report = CommandSpec.builder()
                .executor(new Report())
                .description(Text.of("Generates an ActiveTime report"))
                .permission("activetime.report.base")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.user(Text.of("user"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("days"))))
                .build();
        CommandSpec ActiveTime = CommandSpec.builder()
                .executor(new Base())
                .description(Text.of("Opens the in-game documentation"))
                .permission("activetime.base")
                .child(Check, "Check", "Time")
                .child(Leaderboard, "Leaderboard", "Rank", "Top")
                .child(Report, "Report", "Info")
                .build();
        Sponge.getCommandManager().register(plugin, ActiveTime, Lists.newArrayList("ActiveTime", "aTime"));
        Sponge.getCommandManager().register(plugin, Check, Lists.newArrayList("PlayTime", "OnTime"));
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        if (Sponge.getPluginManager().isLoaded("nucleus")) {
            NucleusIntegration.RegisterMessageToken();
            nucleusEnabled = true;
        } else {
            logger.warn("Nucleus could not be found! Disabling Nucleus support.");
        }
    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        Util.startTasks();
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        Util.initialize();
        Util.startTasks();
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @Root Player player) {
        Util.startNameTask(player);
        if (Config.limitInterval > 0) {
            Task.builder()
                    .name("ActiveTime CheckPlayerLimit Task (Async Processor)")
                    .execute(task -> Util.checkLimit(player))
                    .async()
                    .submit(ActiveTime.getPlugin());
        }
    }

}