package com.mcsimonflash.sponge.activetime.commands;

import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DailyReport implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String text = args.<String>getOne("date").orElse(null);
        try {
            Util.sendPagination(src, "Daily Report: " + text, Storage.getDailyReport(text != null ? LocalDate.parse(text) : LocalDate.now()).print());
            return CommandResult.success();
        } catch (DateTimeParseException e) {
            Util.sendMessage(src, "Error parsing date " + text + ": " + e.getMessage());
            return CommandResult.empty();
        }
    }

}