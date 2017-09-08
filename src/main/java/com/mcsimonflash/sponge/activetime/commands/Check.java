package com.mcsimonflash.sponge.activetime.commands;

import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import com.mcsimonflash.sponge.activetime.objects.TimeWrapper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class Check implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("user").orElse(null);

        if (user != null || (src instanceof User)) {
            user = user == null ? (User) src : user;
            if (user.getName().equals(src.getName()) || src.hasPermission("activetime.check.other")) {
                if (user.hasPermission("activetime.log.base")) {
                    PaginationList.builder()
                            .padding(Text.of(TextColors.AQUA, "="))
                            .title(Text.of(user.getName(), "'s Activity"))
                            .contents(Util.toText(Util.printTime(new TimeWrapper(Storage.getTotalTime(user.getUniqueId(), true), Storage.getTotalTime(user.getUniqueId(), false)))))
                            .sendTo(src);
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