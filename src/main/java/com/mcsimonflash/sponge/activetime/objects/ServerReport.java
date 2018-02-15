package com.mcsimonflash.sponge.activetime.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.text.Text;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServerReport {

    public final LocalDate from, to;
    public final TimeHolder total = new TimeHolder(), dailyAverage = new TimeHolder(), weeklyAverage = new TimeHolder(), monthlyAverage = new TimeHolder();
    public final Map<UUID, UserReport> userReports = Maps.newHashMap();
    public final List<Text> print = Lists.newArrayList();

    public ServerReport(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    public ServerReport generate() {
        if (userReports.isEmpty()) {
            Storage.generateServerReport(this);
        }
        return this;
    }

    public List<Text> print() {
        if (print.isEmpty()) {
            print.add(Util.toText("From: &b" + from));
            print.add(Util.toText("To: &b" + to));
            print.add(Util.toText("Total: &b" + Util.printTime(total)));
            print.add(Util.toText("Daily Average: &b" + Util.printTime(dailyAverage)));
            print.add(Util.toText("Weekly Average: &b" + Util.printTime(weeklyAverage)));
            print.add(Util.toText("Monthly Average: &b" + Util.printTime(monthlyAverage)));
            Util.addTimes(print, "Users: ", userReports.entrySet().stream().map(e -> Maps.immutableEntry(e.getValue().name, e.getValue().total)).sorted(Comparator.comparingInt(e -> -e.getValue().getActiveTime())).collect(Collectors.toList()), n -> n);
        }
        return print;
    }

}
