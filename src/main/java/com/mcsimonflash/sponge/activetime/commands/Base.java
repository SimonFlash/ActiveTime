package com.mcsimonflash.sponge.activetime.commands;

import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.activetime.ActiveTime;
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
                commands.add(Text.builder("/ActiveTime Check ")
                        .color(TextColors.WHITE)
                        .onClick(TextActions.suggestCommand("/ActiveTime Check "))
                        .onHover(TextActions.showText(Text.of(
                                TextColors.WHITE, "Check: ", TextColors.AQUA, "Shows a users's active time\n",
                                TextColors.WHITE, "Aliases: ", TextColors.AQUA, "Check, /PlayTime, /OnTime\n",
                                TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.check.base, activetime.check.other")))
                        .append(Text.builder("[Opt-User]")
                                .color(TextColors.AQUA)
                                .onHover(TextActions.showText(Text.of(
                                        TextColors.WHITE, "Opt-User<User>: ", TextColors.AQUA, "Name of the user")))
                                .build())
                        .build());
            } else {
                commands.add(Text.builder("/ActiveTime Check")
                        .color(TextColors.WHITE)
                        .onClick(TextActions.suggestCommand("/ActiveTime Check"))
                        .onHover(TextActions.showText(Text.of(
                                TextColors.WHITE, "Check: ", TextColors.AQUA, "Shows your active time\n",
                                TextColors.WHITE, "Aliases: ", TextColors.AQUA, "Check, /PlayTime, /OnTime\n",
                                TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.check.base")))
                        .build());
            }
        }
        if (src.hasPermission("activetime.report.base")) {
            commands.add(Text.builder("/ActiveTime Report ")
                    .color(TextColors.WHITE)
                    .onClick(TextActions.suggestCommand("/ActiveTime Report "))
                    .onHover(TextActions.showText(Text.of(
                            TextColors.WHITE, "Report: ", TextColors.AQUA, "Generates an ActiveTime report\n",
                            TextColors.WHITE, "Aliases: ", TextColors.AQUA, "Report\n",
                            TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.report.base")))
                    .append(Text.builder("<User>")
                            .color(TextColors.AQUA)
                            .onHover(TextActions.showText(Text.of(
                                    TextColors.WHITE, "User<User>: ", TextColors.AQUA, "Name of the user")))
                            .build())
                    .build());
        }
        if (src.hasPermission("activetime.top.base")) {
            commands.add(Text.builder("/ActiveTime Leaderboard ")
                    .color(TextColors.WHITE)
                    .onClick(TextActions.suggestCommand("/ActiveTime Leaderboard "))
                    .onHover(TextActions.showText(Text.of(
                            TextColors.WHITE, "Check: ", TextColors.AQUA, "Shows the most active players\n",
                            TextColors.WHITE, "Aliases: ", TextColors.AQUA, "Leaderboard\n",
                            TextColors.WHITE, "Permission: ", TextColors.AQUA, "activetime.top.base")))
                    .append(Text.builder("[Opt-Positions]")
                            .color(TextColors.AQUA)
                            .onHover(TextActions.showText(Text.of(
                                    TextColors.WHITE, "Opt-Positions<Integer>: ", TextColors.AQUA, "Number of positions to display")))
                            .build())
                    .build());
        }
        Text wikiDisc = Text.EMPTY;
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
                .padding(Text.of(TextColors.DARK_BLUE, "="))
                .title(Text.of(TextColors.BLUE, "ActiveTime"))
                .contents(commands)
                .footer(wikiDisc)
                .sendTo(src);
        return CommandResult.success();
    }
}
