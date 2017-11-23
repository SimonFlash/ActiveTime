package com.mcsimonflash.sponge.activetime.commands;

import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class Report implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("user").get();
        Integer days = args.<Integer>getOne("days").get();

        if (user.hasPermission("activetime.log.base")) {
            if (days >= 0) {
                PaginationList.builder()
                        .padding(Text.of(TextColors.AQUA, "="))
                        .title(Text.of(user.getName(), "'s Report"))
                        .contents(Storage.getReport(user.getUniqueId(), days).print())
                        .sendTo(src);
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