package com.mcsimonflash.sponge.activetime.commands.elements;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateElement extends CommandElement {

    public DateElement(@Nullable Text key) {
        super(key);
    }

    @Override
    public Object parseValue(CommandSource src, CommandArgs args) throws ArgumentParseException {
        String arg = args.next();
        try {
            return LocalDate.parse(arg);
        } catch (DateTimeParseException e) {
            throw args.createError(Text.of("Failed to parse ", arg, " as a date: " + e.getMessage()));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Stream.of(LocalDate.now().toString())
                .filter(s -> s.startsWith(args.nextIfPresent().orElse("")))
                .collect(Collectors.toList());
    }

}