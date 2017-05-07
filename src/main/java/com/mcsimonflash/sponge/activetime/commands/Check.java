package com.mcsimonflash.sponge.activetime.commands;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.managers.Config;
import com.mcsimonflash.sponge.activetime.managers.LogTime;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

public class Check implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("user").isPresent() ? args.<User>getOne("user").get() : null;

        if (user != null || (src instanceof Player)) {
            user = user == null ? (User) src : user;
            if (user.getName().equals(src.getName()) || user.hasPermission("activetime.check.other")) {
                if (user.hasPermission("activetime.log")) {
                    if (user.isOnline() && LogTime.activeTimeMap.containsKey(user.getPlayer().get())) {
                        LogTime.saveTime(user.getPlayer().get());
                    }
                    int time = Config.getTime(user);
                    if (time > 0) {
                        Util.sendSrcMsg(src, Util.toText("&b" + user.getName() + " &fhas logged " + Util.printTime(time)), true);
                    } else {
                        Util.sendSrcMsg(src, Util.toText("&b" + user.getName() + " &fhas not logged any time!"), true);
                    }
                    return CommandResult.success();
                } else {
                    Util.sendSrcMsg(src, Util.toText("&b" + user.getName() + " &fis not being logged!"), true);
                    return CommandResult.empty();
                }
            } else {
                Util.sendSrcMsg(src, Util.toText("You do not have permission to check other player's active time!"), true);
                return CommandResult.empty();
            }
        } else {
            Util.sendSrcMsg(src, Util.toText("A playername must be specified!"), true);
            return CommandResult.empty();
        }
    }
}
