package com.mcsimonflash.sponge.activetime.objects;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.managers.Storage;
import com.mcsimonflash.sponge.activetime.managers.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Milestone {

    private String name;
    private int activetime;
    private List<String> commands;

    public Milestone(String name, int activetime, List<String> commands) {
        this.name = name;
        this.activetime = activetime;
        this.commands = commands;
    }

    public void process(Player player, int time) {
        if (time >= activetime && !Storage.hasMilestone(player.getUniqueId(), name)) {
            if (Storage.setMilestone(player.getUniqueId(), name, true)) {
                for (String command : commands) {
                    String modifiedCommand = command.replace("<player>", player.getName()).replace("<activetime>", Integer.toString(time));
                    Util.createTask("ActiveTime GiveMilestone Sync Processor (" + player.getName() + ")", task -> Sponge.getCommandManager().process(Sponge.getServer().getConsole(), modifiedCommand), 0, false);
                }
            } else {
                ActiveTime.getPlugin().getLogger().error("Unable to save obtained milestone! | Milestone:[" + name + "] Player:[" + player + "]");
            }
        }
    }

}