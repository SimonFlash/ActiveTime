package com.mcsimonflash.sponge.activetime.api;


import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import com.mcsimonflash.sponge.activetime.objects.Report;
import com.mcsimonflash.sponge.activetime.objects.TimeWrapper;
import org.spongepowered.api.entity.living.player.User;

import java.time.LocalDate;
import java.util.Optional;

public class ActiveTimeAPI {

    /**
     * Returns the total activetime and afktime of the given user.
     *
     * @param user the user
     * @return the times, in seconds
     */
    public static TimeWrapper getTotalTime(User user) {
        return Storage.getTotalTime(user.getUniqueId());
    }

    /**
     * Returns the daily activetime and afktime of the given user (today, as
     * defined by {@link LocalDate#now()}).
     *
     * @param user the user
     * @return the times, in seconds
     */
    public static TimeWrapper getDailyTime(User user) {
        return Storage.getDailyTime(user.getUniqueId());
    }

    /**
     * Returns the activetime and afktime of the given user for the specified
     * date. If there are no logs for this date or the config could not be
     * successfully loaded, {@link Optional#empty()} is returned.
     *
     * @param user the user
     * @return the times, in seconds
     */
    public static Optional<TimeWrapper> getTime(User user, LocalDate date) {
        return Storage.getTime(user.getUniqueId(), date);
    }

    /**
     * Returns the total amount of a time a user is allowed to be on the server,
     * in seconds.
     *
     * @param user the user
     * @return the playtime limit, if present
     */
    public static Optional<Integer> getDailyPlayTime(User user) {
        return user.getOption("playtime").map(Util::parseTime);
    }

    /**
     * Returns a report of this users activity over the given number of days.
     * The report also includes the current day automatically, meaning that a
     * report for 1 day includes both yesterday and today.
     *
     * @param user the user
     * @param days the number of days to include
     * @return the generated report
     */
    public static Report getReport(User user, int days) {
        return Storage.getReport(user.getUniqueId(), days);
    }

}