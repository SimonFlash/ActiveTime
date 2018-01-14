package com.mcsimonflash.sponge.activetime.commands;

import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;

import java.time.LocalDate;

public class Report implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("user").get();
        if (!user.hasPermission("activetime.log.base")) {
            Util.sendMessage(src, "Notice: " + (user == src ? "Your" : user.getName() + "'s") + " time is not currently being logged.");
        }
        int days = args.<Integer>getOne("days").orElseGet(() -> LocalDate.now().getDayOfMonth() - 1);
        if (days < 0 || days > 365) {
            Util.sendMessage(src, "Days must be within &b0 &fand &b365&f.");
            return CommandResult.empty();
        }
        Util.sendPagination(src, "User Report: " + user.getName(), Storage.getReport(user.getUniqueId(), days).print());
        return CommandResult.success();
    }

}