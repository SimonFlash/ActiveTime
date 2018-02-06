package com.mcsimonflash.sponge.activetime.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.text.Text;

import java.time.LocalDate;
import java.util.*;

public class UserReport {

    public final UUID uuid;
    public final String name;
    public final LocalDate from, to;
    public final TimeHolder total = new TimeHolder(), dailyAverage = new TimeHolder(), weeklyAverage = new TimeHolder(), monthlyAverage = new TimeHolder();
    public final Map<LocalDate, TimeHolder> dailyTimes = Maps.newLinkedHashMap(), weeklyTimes = Maps.newLinkedHashMap(), monthlyTimes = Maps.newLinkedHashMap();
    public final List<Text> print = Lists.newArrayList();

    public UserReport(UUID uuid, LocalDate from, LocalDate to) {
        this.uuid = uuid;
        this.from = from;
        this.to = to;
        name = Storage.getUsername(uuid);
    }

    public UserReport generate() {
        if (dailyTimes.isEmpty()) {
            Storage.generateUserReport(this);
        }
        return this;
    }

    public List<Text> print() {
        if (print.isEmpty()) {
            print.add(Util.toText("Name: &b" + name));
            print.add(Util.toText("UUID: &b" + uuid));
            print.add(Util.toText("From: &b" + from));
            print.add(Util.toText("To: &b" + to));
            print.add(Util.toText("Total: " + Util.printTime(total)));
            print.add(Util.toText("Daily Average: " + Util.printTime(dailyAverage)));
            print.add(Util.toText("Weekly Average: " + Util.printTime(weeklyAverage)));
            print.add(Util.toText("Monthly Average: " + Util.printTime(monthlyAverage)));
            Util.addTimes(print,"Days: ", dailyTimes.entrySet(), Util::printDate);
            Util.addTimes(print, "Weeks: ", weeklyTimes.entrySet(), Util::printDate);
            Util.addTimes(print, "Months: ", monthlyTimes.entrySet(), Util::printDate);
        }
        return print;
    }

}