package com.mcsimonflash.sponge.activetime.objects;

import com.mcsimonflash.sponge.activetime.ActiveTime;
import com.mcsimonflash.sponge.activetime.managers.Storage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public class Milestone {

    private String name;
    private int activetime;
    private String command;

    public Milestone(String name, int activetime, String command) {
        this.name = name;
        this.activetime = activetime;
        this.command = command;
    }

    public void process(Player player, int activetime) {
        if (activetime >= this.activetime && !Storage.hasMilestone(player.getUniqueId(), name)) {
            if (Storage.setMilestone(player.getUniqueId(), name, true)) {
                String modifiedCommand = command.replace("<player>", player.getName()).replace("<activetime>", Integer.toString(activetime));
                Task.builder()
                        .name("ActiveTime Milestone Task: " + player.getName() + "(Sync Processor")
                        .execute(task -> Sponge.getCommandManager().process(Sponge.getServer().getConsole(), modifiedCommand))
                        .submit(ActiveTime.getPlugin());
            } else {
                ActiveTime.getPlugin().getLogger().error("Unable to save obtained milestone! | Milestone:[" + name + "] Player:[" + player + "]");
            }
        }
    }
}
