package com.mcsimonflash.sponge.activetime.commands;

import com.google.common.collect.ImmutableMap;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Base implements CommandExecutor {

    public static final CommandSpec SPEC = CommandSpec.builder()
            .executor(new Base())
            .child(Check.SPEC, "check", "info", "time")
            .child(Leaderboard.SPEC, "leaderboard", "rank", "top")
            .child(GenerateReport.SPEC, "generatereport", "report")
            .description(Text.of("Opens the in-game documentation"))
            .permission("activetime.base")
            .build();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        (src instanceof Player ? PaginationList.builder() : PaginationList.builder().linesPerPage(-1))
                .padding(Text.of(TextColors.AQUA, "="))
                .title(Text.of("ActiveTime"))
                .contents(USAGES.entrySet().stream().filter(e -> src.hasPermission(e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList()))
                .footer(WIKI_DISC)
                .build()
                .sendTo(src);
        return CommandResult.success();
    }

    private static final ImmutableMap<String, Text> USAGES = ImmutableMap.<String, Text>builder()
            .put("activetime.base", usage("/activetime "))
            .put("activetime.check.base", usage("/activetime check ", arg(false, "user", "Name of the user to check")))
            .put("activetime.putall.base", usage("/activetime dailyreport ", arg(false, "date", "Date of the daily report")))
            .put("activetime.leaderboard.base", usage("/activetime leaderboard ", arg(false, "positions", "Number of positions to include")))
            .put("activetime.report.base", usage("/activetime report ", arg(false, "user", "Name of the user for the report"), arg(false, "days", "Number of days to include")))
            .build();
    private static final Text WIKI_DISC = Text.of("| ", link("ActiveTime Wiki", Optional.ofNullable(ActiveTime.getWiki())), " | ", link("Support Discord", Optional.ofNullable(ActiveTime.getDiscord())), " |");

    private static Text usage(String base, Text... args) {
        return Text.builder(base)
                .color(TextColors.WHITE)
                .onClick(TextActions.suggestCommand(base))
                .append(Text.joinWith(Text.of(" "), args))
                .build();
    }

    private static Text arg(boolean req, String name, String desc) {
        return Text.builder((req ? "<" : "[") + name + (req ? ">" : "]"))
                .color(TextColors.AQUA)
                .onHover(TextActions.showText(Text.of(TextColors.WHITE, name, ": ", TextColors.GRAY, desc)))
                .build();
    }

    private static Text link(String name, Optional<URL> optUrl) {
        return optUrl.map(u -> Text.builder(name)
                .style(TextStyles.UNDERLINE)
                .onClick(TextActions.openUrl(u))
                .onHover(TextActions.showText(Text.of(u)))
                .build()).orElse(Text.builder(name)
                .color(TextColors.RED)
                .onHover(TextActions.showText(Text.of(TextColors.RED, "Sorry! This URL is unavailable.")))
                .build());
    }

}
