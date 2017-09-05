package com.mcsimonflash.sponge.activetime.objects;

public class TimeWrapper {

    private int activetime;
    private int afktime;

    public TimeWrapper() {
        this(0, 0);
    }

    public TimeWrapper(int activetime, int afktime) {
        set(activetime, afktime);
    }

    public void set(int activetime, int afktime) {
        this.activetime = activetime;
        this.afktime = afktime;
    }

    public void add(int activetime, int afktime) {
        this.activetime += activetime;
        this.afktime += afktime;
    }

    public void add(TimeWrapper time) {
        add(time.getActivetime(), time.getAfktime());
    }

    public int getActivetime() {
        return activetime;
    }

    public int getAfktime() {
        return afktime;
    }

}