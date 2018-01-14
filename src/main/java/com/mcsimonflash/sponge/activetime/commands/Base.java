package com.mcsimonflash.sponge.activetime.commands;

import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.managers.Config;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;

public class Base implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        List<Text> commands = Lists.newArrayList();
        if (src.hasPermission("activetime.check.base")) {
            if (src.hasPermission("activetime.check.other")) {
                commands.add(Text.builder("/activeTime check ")
                        .color(TextColors.WHITE)
                        .onClick(TextActions.suggestCommand("/activeTime check "))
                        .onHover(TextActions.showText(Text.of(
                                TextColors.WHITE, "Check: ", TextColors.AQUA, "Shows a users's active time\n",
                                TextColors.WHITE, "Aliases: ", TextColors.AQUA, "check, /playtime, /ontime\n",
                                TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.check.base, activetime.check.other")))
                        .append(Text.builder("[User]")
                                .color(TextColors.AQUA)
                                .onHover(TextActions.showText(Text.of(
                                        TextColors.WHITE, "User: ", TextColors.AQUA, "Name of the user, defaults to yourself")))
                                .build())
                        .build());
            } else {
                commands.add(Text.builder("/activetime check")
                        .color(TextColors.WHITE)
                        .onClick(TextActions.suggestCommand("/activetime check"))
                        .onHover(TextActions.showText(Text.of(
                                TextColors.WHITE, "Check: ", TextColors.AQUA, "Shows your active time\n",
                                TextColors.WHITE, "Aliases: ", TextColors.AQUA, "check, /playtime, /ontime\n",
                                TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.check.base")))
                        .build());
            }
        }
        if (src.hasPermission("activetime.leaderboard.base")) {
            commands.add(Text.builder("/activetime leaderboard ")
                    .color(TextColors.WHITE)
                    .onClick(TextActions.suggestCommand("/activetime leaderboard "))
                    .onHover(TextActions.showText(Text.of(
                            TextColors.WHITE, "Check: ", TextColors.AQUA, "Shows the most active players\n",
                            TextColors.WHITE, "Aliases: ", TextColors.AQUA, "leaderboard\n",
                            TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.top.base")))
                    .append(Text.builder("[Positions]")
                            .color(TextColors.AQUA)
                            .onHover(TextActions.showText(Text.of(
                                    TextColors.WHITE, "Integer: ", TextColors.AQUA, "Number of positions to display (1 - ", Config.maximumPos, "), default ",Config.defaultPos)))
                            .build())
                    .build());
        }
        if (src.hasPermission("activetime.report.base")) {
            commands.add(Text.builder("/activetime report ")
                    .color(TextColors.WHITE)
                    .onClick(TextActions.suggestCommand("/activetime report "))
                    .onHover(TextActions.showText(Text.of(
                            TextColors.WHITE, "Report: ", TextColors.AQUA, "Generates an ActiveTime report\n",
                            TextColors.WHITE, "Aliases: ", TextColors.AQUA, "report\n",
                            TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.report.base")))
                    .append(Text.builder("[User] ")
                            .color(TextColors.AQUA)
                            .onHover(TextActions.showText(Text.of(
                                    TextColors.WHITE, "User: ", TextColors.AQUA, "Name of the user, defaults to yourself")))
                            .build())
                    .append(Text.builder("[Days]")
                            .color(TextColors.AQUA)
                            .onHover(TextActions.showText(Text.of(
                                    TextColors.WHITE, "Integer: ", TextColors.AQUA, "Number of days to include, defaults to day of the month (displays this months data)")))
                            .build())
                    .build());
        }
        if (src.hasPermission("activetime.dailyreport.base")) {
            commands.add(Text.builder("/activetime dailyreport ")
                    .color(TextColors.WHITE)
                    .onClick(TextActions.suggestCommand("/activetime dailyreport "))
                    .onHover(TextActions.showText(Text.of(
                            TextColors.WHITE, "DailyReport: ", TextColors.AQUA, "Generates a daily report\n",
                            TextColors.WHITE, "Aliases: ", TextColors.AQUA, "dailyreport\n",
                            TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.dailyreport.base")))
                    .append(Text.builder("[Date]")
                            .color(TextColors.AQUA)
                            .onHover(TextActions.showText(Text.of(
                                    TextColors.WHITE, "String: ", TextColors.AQUA, "The date of the report (yyyy-mm-dd), defaults to today")))
                            .build())
                    .build());
        }
        Text wikiDisc = null;
        if (ActiveTime.getWiki() != null && ActiveTime.getDiscord() != null) {
            wikiDisc = Text.builder("| ")
                    .color(TextColors.WHITE)
                    .append(Text.builder("ActiveTime Wiki")
                            .color(TextColors.AQUA).style(TextStyles.UNDERLINE)
                            .onClick(TextActions.openUrl(ActiveTime.getWiki()))
                            .onHover(TextActions.showText(Text.of("Click to open the ActiveTime Wiki")))
                            .build())
                    .append(Text.of(TextColors.WHITE, " | "))
                    .append(Text.builder("Support Discord")
                            .color(TextColors.AQUA).style(TextStyles.UNDERLINE)
                            .onClick(TextActions.openUrl(ActiveTime.getDiscord()))
                            .onHover(TextActions.showText(Text.of("Click to open the Support Discord")))
                            .build())
                    .append(Text.of(TextColors.WHITE, " |"))
                    .build();
        }
        PaginationList.builder()
                .padding(Text.of(TextColors.AQUA, "="))
                .title(Text.of("ActiveTime"))
                .contents(commands)
                .footer(wikiDisc)
                .sendTo(src);
        return CommandResult.success();
    }
}
