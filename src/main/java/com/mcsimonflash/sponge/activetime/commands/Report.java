package com.mcsimonflash.sponge.activetime.commands;

import com.mcsimonflash.sponge.activetime.commands.elements.DateElement;
import com.mcsimonflash.sponge.activetime.commands.elements.FlagsElement;
import com.mcsimonflash.sponge.activetime.managers.Config;
import com.mcsimonflash.sponge.activetime.managers.Util;
import com.mcsimonflash.sponge.activetime.objects.ServerReport;
import com.mcsimonflash.sponge.activetime.objects.UserReport;
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
import java.time.temporal.ChronoUnit;

public class Report implements CommandExecutor {

    public static final CommandSpec SPEC = CommandSpec.builder()
            .executor(new Report())
            .arguments(FlagsElement.builder()
                    .flag("server", "s")
                    .flag(new DateElement(Text.of("from")), "from", "f")
                    .flag(new DateElement(Text.of("to")), "to", "t")
                    .flag(GenericArguments.user(Text.of("user")), "user", "u")
                    .build())
            .description(Text.of("Generates an ActiveTime report"))
            .permission("activetime.report.base")
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        LocalDate from = args.<LocalDate>getOne("from").orElseGet(() -> LocalDate.now().withDayOfMonth(1));
        LocalDate to = args.<LocalDate>getOne("to").orElseGet(LocalDate::now);
        if (to.isBefore(from)) {
            throw new CommandException(Util.toText("&fThe from date &b" + from.toString() + "&f must be before the to date &b" + to.toString() + "&f."));
        } else if (ChronoUnit.DAYS.between(from, to) + 1 > Config.maximumRep) {
            throw new CommandException(Util.toText("&fThe range of dates must not exceed the maximum report size of &b" + Config.maximumRep + " &fdays."));
        } else if (args.hasAny("server")) {
            if (!src.hasPermission("activetime.report.server")) {
                throw new CommandException(Util.toText("&fYou do not have permission to generate a server report."));
            }
            Util.sendMessage(src, "Generating server report between &b" + from + " &fand &b" + to + ".");
            Util.createTask("ActiveTime Server Report Generator (" + src.getName() + ")", t -> Util.sendPagination(src, "Server Activity", new ServerReport(from, to).generate().print()), 0, true);
        } else if (args.hasAny("user") || (src instanceof User)) {
            User user = args.<User>getOne("user").orElseGet(() -> (User) src);
            if (user != src && !src.hasPermission("activetime.report.other")) {
                throw new CommandException(Util.toText("&fYou do not have permission to generate a report for another user."));
            }
            Util.sendMessage(src, "Generating user report for &b" + user.getName() + " &fbetween &b" + from + " &fand &b" + to + ".");
            Util.createTask("ActiveTime User Report Generator (" + src.getName() + ")", t -> Util.sendPagination(src, user.getName() + "'s Activity", new UserReport(user.getUniqueId(), from, to).generate().print()), 0, true);
        } else {
            throw new CommandException(Util.toText("&fA user must be defined to use this command from console."));
        }
        return CommandResult.success();
    }

}