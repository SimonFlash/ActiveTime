package com.mcsimonflash.sponge.activetime.objects;

import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.LocalDate;
import java.util.TreeMap;
import java.util.UUID;

public class Report {

    public boolean error;
    public String name;
    public UUID uuid;
    public TreeMap<LocalDate, TimeWrapper> dailyTimes = Maps.newTreeMap();
    public TreeMap<LocalDate, TimeWrapper> weeklyTimes = Maps.newTreeMap();
    public TreeMap<LocalDate, TimeWrapper> monthlyTimes = Maps.newTreeMap();
    public TimeWrapper total = new TimeWrapper();
    public TimeWrapper dailyAverage = new TimeWrapper();
    public TimeWrapper weeklyAverage = new TimeWrapper();
    public TimeWrapper monthlyAverage = new TimeWrapper();

    public Text print() {
        Text.Builder builder = Text.builder();
        builder.append(Text.of("ActiveTime Report: ", TextColors.AQUA, name));
        builder.append(Text.of("UUID: ", TextColors.AQUA, uuid));
        builder.append(Text.of("Total: ", TextColors.AQUA, Util.printTime(total)));
        TimeWrapper[] days = dailyTimes.values().toArray(new TimeWrapper[0]);
        if (days.length >= 1) {
            builder.append(Text.of("Today: ", TextColors.AQUA, Util.printTime(days[days.length-1])));
        }
        if (days.length >= 2) {
            builder.append(Text.of("Yesterday: ", TextColors.AQUA, Util.printTime(days[days.length-2])));
        }
        builder.append(Text.of("Daily Average: ", TextColors.AQUA, Util.printTime(dailyAverage)));
        TimeWrapper[] weeks = weeklyTimes.values().toArray(new TimeWrapper[0]);
        if (weeks.length >= 1) {
            builder.append(Text.of("This Week: ", TextColors.AQUA, Util.printTime(weeks[weeks.length-1])));
        }
        if (weeks.length >= 2) {
            builder.append(Text.of("Last Week: ", TextColors.AQUA, Util.printTime(weeks[weeks.length-2])));
        }
        builder.append(Text.of("Weekly Average: ", TextColors.AQUA, Util.printTime(weeklyAverage)));
        TimeWrapper[] months = monthlyTimes.values().toArray(new TimeWrapper[0]);
        if (months.length >= 1) {
            builder.append(Text.of("This Month: ", TextColors.AQUA, Util.printTime(months[months.length-1])));
        }
        if (months.length >= 2) {
            builder.append(Text.of("Last Month: ", TextColors.AQUA, Util.printTime(months[months.length-2])));
        }
        builder.append(Text.of("Monthly Average: ", TextColors.AQUA, Util.printTime(monthlyAverage)));
        return builder.build();
    }

}