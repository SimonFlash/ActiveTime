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

public class Check implements CommandExecutor {

    public static final CommandSpec SPEC = CommandSpec.builder()
            .executor(new Check())
            .arguments(GenericArguments.userOrSource(Text.of("user")))
            .description(Text.of("Shows a player's ActiveTime status"))
            .permission("activetime.check.base")
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("user").get();
        if (user != src && !src.hasPermission("activetime.check.other")) {
            Util.sendMessage(src, "You do not have permission to check another player's active time!");
            return CommandResult.empty();
        } else if (!user.hasPermission("activetime.log.base")) {
            Util.sendMessage(src, "Notice: " + (user == src ? "Your" : user.getName() + "'s") + " time is not currently being logged.");
        }
        Util.sendPagination(src, user.getName() + "'s Activity", Lists.newArrayList(Util.toText(Util.printTime(Storage.getTotalTime(user.getUniqueId())))));
        return CommandResult.success();
    }
}