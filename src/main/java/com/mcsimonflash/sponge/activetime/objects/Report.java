package com.mcsimonflash.sponge.activetime.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.text.Text;

import java.time.LocalDate;
import java.util.*;

public class Report {

    public boolean error;
    public String name;
    public UUID uuid;
    public TreeMap<LocalDate, TimeWrapper> dailyTimes = Maps.newTreeMap();
    public TreeMap<LocalDate, TimeWrapper> weeklyTimes = Maps.newTreeMap();
    public TreeMap<LocalDate, TimeWrapper> monthlyTimes = Maps.newTreeMap();
    public TimeWrapper total = new TimeWrapper(0, 0);
    public TimeWrapper dailyAverage = new TimeWrapper(0, 0);
    public TimeWrapper weeklyAverage = new TimeWrapper(0, 0);
    public TimeWrapper monthlyAverage = new TimeWrapper(0, 0);

    public List<Text> print() {
        List<Text> texts = Lists.newArrayList();
        texts.add(Util.toText("Name: &b" + name));
        texts.add(Util.toText("UUID: &b" + uuid));
        texts.add(Util.toText("Total: " + Util.printTime(total)));
        texts.add(Util.toText("Daily Average: " + Util.printTime(dailyAverage)));
        texts.add(Util.toText("Weekly Average: " + Util.printTime(weeklyAverage)));
        texts.add(Util.toText("Monthly Average: " + Util.printTime(monthlyAverage)));
        texts.add(Util.toText("Today &7(" + Util.printDate(dailyTimes.lastKey()) + ")&f: " + Util.printTime(dailyTimes.lastEntry().getValue())));
        texts.add(Util.toText("This Week &7(" + Util.printDate(weeklyTimes.lastKey()) + ")&f: " + Util.printTime(weeklyTimes.lastEntry().getValue())));
        texts.add(Util.toText("This Month &7(" + Util.printDate(monthlyTimes.lastKey()) + ")&f: " + Util.printTime(monthlyTimes.lastEntry().getValue())));
        texts.add(Util.toText("Days:"));
        addTimes(texts, dailyTimes);
        texts.add(Util.toText("Weeks:"));
        addTimes(texts, weeklyTimes);
        texts.add(Util.toText("Months:"));
        addTimes(texts, monthlyTimes);
        return texts;
    }

    private static void addTimes(List<Text> texts, TreeMap<LocalDate, TimeWrapper> times) {
        times.entrySet().stream().filter(e -> e.getValue().getActiveTime() != 0 || e.getValue().getAfkTime() != 0).forEach(e -> texts.add(Util.toText(" - &7" + Util.printDate(e.getKey()) + "&f: " + Util.printTime(e.getValue()))));
    }

}