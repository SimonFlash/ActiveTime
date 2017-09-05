package com.mcsimonflash.sponge.activetime.commands;

import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;

public class Report implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("user").get();
        Integer days = args.<Integer>getOne("days").get();

        if (user.hasPermission("activetime.log.base")) {
            if (days >= 1) {
                src.sendMessage(Storage.buildReport(user.getUniqueId(), days).print());
                return CommandResult.success();
            } else {
                src.sendMessage(Util.prefix.concat(Util.toText("Days must be at least 1!")));
            }
        } else {
            src.sendMessage(Util.prefix.concat(Util.toText("&b" + user.getName() + " &fis not being logged!")));
        }
        return CommandResult.empty();
    }

}