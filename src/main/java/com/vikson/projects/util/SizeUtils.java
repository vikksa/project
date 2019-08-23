package com.vikson.projects.util;

public class SizeUtils {

    private SizeUtils() {
    }

    //Source: http://programming.guide/java/formatting-byte-size-to-human-readable-format.html
    public static String getHumanReadable(long size) {
        int unit = 1000;
        if (size < unit) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", size / Math.pow(unit, exp), pre);
    }
}
