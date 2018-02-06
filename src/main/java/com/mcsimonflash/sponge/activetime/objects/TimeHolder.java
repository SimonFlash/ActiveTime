package com.mcsimonflash.sponge.activetime.objects;

public class TimeHolder {

    private int activetime = 0, afktime = 0;

    public TimeHolder() {}

    public TimeHolder(int activetime, int afktime) {
        set(activetime, afktime);
    }

    public TimeHolder set(int activetime, int afktime) {
        this.activetime = activetime;
        this.afktime = afktime;
        return this;
    }

    public TimeHolder add(int activetime, int afktime) {
        return set(this.activetime + activetime, this.afktime + afktime);
    }

    public TimeHolder add(int time, boolean active) {
        return add(active ? time : 0, active ? 0 : time);
    }

    public TimeHolder add(TimeHolder time) {
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