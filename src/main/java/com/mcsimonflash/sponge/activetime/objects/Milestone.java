package com.mcsimonflash.sponge.activetime.objects;

import com.google.common.collect.ImmutableList;
import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.stream.Collectors;

public class Milestone {

    private final String name;
    private final int activetime;
    private final boolean repeatable;
    private final ImmutableList<String> commands;

    public Milestone(String name, int activetime, boolean repeatable, List<String> commands) {
        this.name = name;
        this.activetime = activetime;
        this.repeatable = repeatable;
        this.commands = ImmutableList.copyOf(commands);
    }

    public void process(Player player, int time) {
        int last = Storage.getMilestoneTime(player.getUniqueId(), this);
        if (repeatable ? time >= activetime + last : last == 0 && time >= activetime) {
            Storage.setMilestoneTime(player.getUniqueId(), this, time);
            List<String> modifiedCommands = commands.stream().map(s -> s.replace("<player>", player.getName()).replace("<activetime>", Util.printTime(time))).collect(Collectors.toList());
            Util.createTask("ActiveTime GiveMilestone Sync Processor (" + player.getName() + ")", task -> modifiedCommands.forEach(c -> Sponge.getCommandManager().process(Sponge.getServer().getConsole(), c)), 0, false);
        }
    }

    public String getName() {
        return name;
    }
    public int getActiveTime() {
        return activetime;
    }
    public boolean isRepeatable() {
        return repeatable;
    }
    public ImmutableList<String> getCommands() {
        return commands;
    }

}