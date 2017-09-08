package com.mcsimonflash.sponge.activetime.commands;


import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.activetime.managers.Config;
import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class Leaderboard implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Integer positions = args.<Integer>getOne("positions").orElse(Config.defaultPositions);

        if (positions > 0 && positions <= Config.maximumPositions) {
            List<Text> results = Lists.newArrayList();
            for (int i = 0; i < Math.min(positions, Storage.leaderboard.size()); i++) {
                results.add(Storage.leaderboard.get(i));
            }
            PaginationList.builder()
                    .padding(Text.of(TextColors.AQUA, "="))
                    .title(Text.of("ActiveTime Leaderboard"))
                    .contents(results)
                    .sendTo(src);
            return CommandResult.success();
        } else {
            src.sendMessage(Util.toText("Range is out of bounds! Must be between &b0 &fand &b" + Config.maximumPositions + "."));
        }
        return CommandResult.empty();
    }
}