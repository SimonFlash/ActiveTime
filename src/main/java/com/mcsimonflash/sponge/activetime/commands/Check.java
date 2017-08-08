package com.mcsimonflash.sponge.activetime.commands;

import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;

public class Check implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("user").orElse(null);

        if (user != null || (src instanceof User)) {
            user = user == null ? (User) src : user;
            if (user.getName().equals(src.getName()) || src.hasPermission("activetime.check.other")) {
                if (user.hasPermission("activetime.log.base")) {
                    src.sendMessage(Util.prefix.concat(Util.toText("&b" + user.getName() + "&f's Log")));
                    src.sendMessage(Util.toText("activetime: " + Util.printTime(Storage.getTotalTime(user.getUniqueId(), true))));
                    src.sendMessage(Util.toText("afktime: " + Util.printTime(Storage.getTotalTime(user.getUniqueId(), false))));
                    return CommandResult.success();
                } else {
                    src.sendMessage(Util.prefix.concat(Util.toText("&b" + user.getName() + " &fis not being logged!")));
                }
            } else {
                src.sendMessage(Util.prefix.concat(Util.toText("You do not have permission to check other player's active time!")));
            }
        } else {
            src.sendMessage(Util.prefix.concat(Util.toText("A playername must be specified!")));
        }
        return CommandResult.empty();
    }
}