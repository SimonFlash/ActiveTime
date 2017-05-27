package com.mcsimonflash.sponge.activetime.managers;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static final int[] timeConst = new int[]{604800, 86400, 3600, 60, 1};
    public static final String[] unitConst = new String[]{"w", "d", "h", "m", "s"};
    public static final Pattern timeFormat = Pattern.compile("^((\\d+)w)?((\\d+)d)?((\\d+)h)?((\\d+)m)?((\\d+)s)?$");

    /**
     * GENERAL
     */

    public static void sendSrcMsg(CommandSource src, Text text, boolean prefix) {
        text = prefix ? Util.toText("&1[&9ActiveTime&1] &f").concat(text) : text;
        src.sendMessage(text);
    }

    public static Text toText(String msg) {
        return TextSerializers.FORMATTING_CODE.deserialize(msg);
    }

    /**
     * TIME
     */

    public static int parseTime(String timeStr) {
        int time = 0;
        try {
            time = Integer.parseInt(timeStr);
        } catch (NumberFormatException ignored) {
            Matcher tf = timeFormat.matcher(timeStr);
            if (tf.matches()) {
                for (int i = 2; i <= 10; i += 2) {
                    if (tf.group(i) != null && tf.group(i).length() > 0) {
                        time += Integer.parseInt(tf.group(i)) * timeConst[i / 2 - 1];
                    }
                }
            }
        }
        return time;
    }

    public static String printTime(int time) {
        StringBuilder timeStr = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (time / timeConst[i] > 0) {
                timeStr.append(time / timeConst[i]).append(unitConst[i]);
                time = time % timeConst[i];
            }
        }
        return timeStr.toString();
    }

    /**
     * PLAYER TIMES
     */

    public static void updatePlayerTimes() {
        Map<String, Integer> playerTimesMap = Config.getPlayerTimesMap();
        List<String> players = Lists.newLinkedList(playerTimesMap.keySet());
        players.sort((o1, o2) -> playerTimesMap.get(o1) > playerTimesMap.get(o2) ? -1 : 1);
        LogTime.playerTimes.clear();
        for (String player : players) {
            LogTime.playerTimes.add("&f" + player + "&b, &f" + Util.printTime(playerTimesMap.get(player)));
        }
    }
}
