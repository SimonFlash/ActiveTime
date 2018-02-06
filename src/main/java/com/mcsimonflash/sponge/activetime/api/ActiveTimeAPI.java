package com.mcsimonflash.sponge.activetime.api;


import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import com.mcsimonflash.sponge.activetime.objects.ServerReport;
import com.mcsimonflash.sponge.activetime.objects.UserReport;
import com.mcsimonflash.sponge.activetime.objects.TimeHolder;
import org.spongepowered.api.entity.living.player.User;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ActiveTimeAPI {

    /**
     * Returns the total activetime and afktime of the given user.
     *
     * @param user The user
     * @return The times, in seconds
     */
    public static TimeHolder getTotalTime(User user) {
        return Storage.getTotalTime(user.getUniqueId());
    }

    /**
     * Returns the daily activetime and afktime of the given user (today, as
     * defined by {@link LocalDate#now()}).
     *
     * @param user The user
     * @return The times, in seconds
     */
    public static TimeHolder getDailyTime(User user) {
        return Storage.getDailyTime(user.getUniqueId());
    }

    /**
     * Returns the activetime and afktime of the given user for the specified
     * date. If there are no logs for this date or the config could not be
     * successfully loaded, {@link Optional#empty()} is returned.
     *
     * @param user The user
     * @return The times, in seconds
     */
    public static Optional<TimeHolder> getTime(User user, LocalDate date) {
        return Storage.getTime(user.getUniqueId(), date);
    }

    /**
     * Returns the total amount of a time a user is allowed to be on the server,
     * in seconds.
     *
     * @param user The user
     * @return The playtime limit, if present
     */
    public static Optional<Integer> getDailyPlayTime(User user) {
        return user.getOption("playtime").map(Util::parseTime);
    }

    /**
     * Gets a report of the user's activity over the given {@link LocalDate}s.
     * It is strongly recommended to generate the report asynchronously, and as
     * such this method returns a {@link CompletableFuture<UserReport>}.
     *
     * @param user The user
     * @param from The date to start the report
     * @param to The date to end the report
     * @return A CompletableFuture with the generated report
     * @deprecated This method is not a stable part of the API, but is provided
     *         for those who wish to use it.
     */
    @Deprecated
    public static CompletableFuture<UserReport> getUserReport(User user, LocalDate from, LocalDate to) {
        return CompletableFuture.supplyAsync(() -> new UserReport(user.getUniqueId(), from, to).generate());
    }

    /**
     * Gets a report of the server's activity over the given {@link LocalDate}s.
     * It is strongly recommended to generate the report asynchronously, and as
     * such this method returns a {@link CompletableFuture<UserReport>}.
     *
     * @param from The date to start the report
     * @param to The date to end the report
     * @return A CompletableFuture with the generated report
     * @deprecated This method is not a stable part of the API, but is provided
     *         for those who wish to use it.
     */
    public static CompletableFuture<ServerReport> getServerReport(LocalDate from, LocalDate to) {
        return CompletableFuture.supplyAsync(() -> new ServerReport(from, to).generate());
    }

}