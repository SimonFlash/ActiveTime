package com.mcsimonflash.sponge.activetime.commands;

import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.time.LocalDate;

public class Check implements CommandExecutor {

    public static final CommandSpec SPEC = CommandSpec.builder()
            .executor(new Check())
            .arguments(GenericArguments.optional(GenericArguments.user(Text.of("user"))))
            .description(Text.of("Shows a player's ActiveTime status"))
            .permission("activetime.check.base")
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!args.hasAny("user") && !(src instanceof User)) {
            throw new CommandException(Util.toText("&fYou must specify a user for this command."));
        }
        User user = args.<User>getOne("user").orElseGet(() -> (User) src);
        if (user != src && !src.hasPermission("activetime.check.other")) {
            throw new CommandException(Util.toText("&fYou do not have permission to check another player's active time!"));
        } else if (!user.hasPermission("activetime.log.base")) {
            Util.sendMessage(src, "Notice: " + (user == src ? "Your" : user.getName() + "'s") + " time is not currently being logged.");
        }
        Util.sendPagination(src, user.getName() + "'s Activity", Lists.newArrayList(Util.toText("Total: " + Util.printTime(Storage.getTotalTime(user.getUniqueId()))), Util.toText("Today &7(" + Util.printDate(LocalDate.now()) + ")&f: " + Util.printTime(Storage.getDailyTime(user.getUniqueId())))));
        return CommandResult.success();
    }

}