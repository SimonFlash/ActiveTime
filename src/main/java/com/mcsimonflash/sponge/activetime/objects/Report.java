package com.mcsimonflash.sponge.activetime.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.text.Text;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Report {

    public boolean error;
    public UUID uuid;
    public String name;
    public TimeWrapper total = new TimeWrapper();
    public TimeWrapper dailyAverage = new TimeWrapper();
    public TimeWrapper weeklyAverage = new TimeWrapper();
    public TimeWrapper monthlyAverage = new TimeWrapper();
    public TreeMap<LocalDate, TimeWrapper> dailyTimes = Maps.newTreeMap();
    public TreeMap<LocalDate, TimeWrapper> weeklyTimes = Maps.newTreeMap();
    public TreeMap<LocalDate, TimeWrapper> monthlyTimes = Maps.newTreeMap();

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
        addTimes(texts,"Days: ", dailyTimes.entrySet(), Util::printDate);
        addTimes(texts, "Weeks: ", weeklyTimes.entrySet(), Util::printDate);
        addTimes(texts, "Months: ", monthlyTimes.entrySet(), Util::printDate);
        return texts;
    }

    private static <T> void addTimes(List<Text> texts, String category, Collection<Map.Entry<T, TimeWrapper>> times, Function<T, String> function) {
        List<Map.Entry<T, TimeWrapper>> filtered = times.stream().filter(e -> e.getValue().getActiveTime() != 0 || e.getValue().getAfkTime() != 0).collect(Collectors.toList());
        texts.add(Util.toText(category + "(" + filtered.size() + ")"));
        filtered.forEach(e -> texts.add(Util.toText(" - &7" + function.apply(e.getKey()) + "&f: &b" + Util.printTime(e.getValue()))));
    }

    public static class Daily {

        public boolean error;
        public LocalDate date;
        public TimeWrapper total = new TimeWrapper();
        public TimeWrapper average = new TimeWrapper();
        public List<Map.Entry<String, TimeWrapper>> userTimes = Lists.newArrayList();

        public List<Text> print() {
            List<Text> texts = Lists.newArrayList();
            texts.add(Util.toText("Date: &b" + Util.printDate(date)));
            texts.add(Util.toText("Total: &b" + Util.printTime(total)));
            texts.add(Util.toText("Average: &b" + Util.printTime(average)));
            addTimes(texts, "Users: ", userTimes, n -> n);
            return texts;
        }

    }

}