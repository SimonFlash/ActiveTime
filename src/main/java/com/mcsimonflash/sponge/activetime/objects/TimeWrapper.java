package com.mcsimonflash.sponge.activetime.objects;

public class TimeWrapper {

    private int activetime;
    private int afktime;

    public TimeWrapper(int activetime, int afktime) {
        set(activetime, afktime);
    }

    public TimeWrapper set(int activetime, int afktime) {
        this.activetime = activetime;
        this.afktime = afktime;
        return this;
    }

    public TimeWrapper add(int activetime, int afktime) {
        return set(this.activetime + activetime, this.afktime + afktime);
    }

    public TimeWrapper add(int time, boolean active) {
        return add(active ? time : 0, active ? 0 : time);
    }

    public TimeWrapper add(TimeWrapper time) {
        return add(time.getActiveTime(), time.getAfkTime());
    }

    public int getActiveTime() {
        return activetime;
    }

    public int getAfkTime() {
        return afktime;
    }

    public int getTime(boolean active) {
        return active ? activetime : afktime;
    }

}