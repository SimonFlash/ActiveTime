package com.mcsimonflash.sponge.activetime.commands;

import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.activetime.managers.Config;
import com.mcsimonflash.sponge.activetime.managers.LogTime;
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

public class Top implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        int range = args.<Integer>getOne("range").isPresent() ? args.<Integer>getOne("range").get() : Config.getDefaultRange();

        if (LogTime.playerTimes.isEmpty()) {
            Util.sendSrcMsg(src, Util.toText("No players to display!"), true);
            return CommandResult.empty();
        }
        if (range < 1) {
            Util.sendSrcMsg(src, Util.toText("Range is too low! Must be at least &b1&f!"), true);
            return CommandResult.empty();
        }
        if (range > Config.getMaxRange()) {
            Util.sendSrcMsg(src, Util.toText("Range is too high! Must be at most &b" + Config.getMaxRange() + "&f!"), true);
            return CommandResult.empty();
        }
        List<Text> times = Lists.newArrayList();
        int max = range > LogTime.playerTimes.size() ? LogTime.playerTimes.size() : range;
        for (int i = 0; i < max; i++) {
            times.add(Util.toText("&b" + (i+1) + ": " + LogTime.playerTimes.get(i)));
        }
        PaginationList.builder()
                .padding(Text.of(TextColors.DARK_BLUE, "="))
                .title(Text.of(TextColors.BLUE, "ActiveTime"))
                .contents(times)
                .sendTo(src);
        return CommandResult.success();
    }
}
