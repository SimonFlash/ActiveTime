package com.mcsimonflash.sponge.activetime.commands;


import com.mcsimonflash.sponge.activetime.managers.Config;
import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.stream.Collectors;

public class Leaderboard implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Integer positions = args.<Integer>getOne("positions").orElse(Config.defaultPos);
        if (positions <= 0 || positions > Config.maximumPos) {
            src.sendMessage(Util.toText("Range must be within &b1 &fand &b" + Config.maximumPos + "&f."));
            return CommandResult.empty();
        }
        Util.sendPagination(src, "ActiveTime Leaderboard", Storage.leaderboard.stream().limit(positions).collect(Collectors.toList()));
        return CommandResult.success();
    }

}