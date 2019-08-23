package com.vikson.projects.util;

import org.springframework.util.StringUtils;

import java.util.UUID;

public class ParseUtils {

    private ParseUtils() {
    }

    public static UUID parseUUIDNullable(String string) {
        if(StringUtils.isEmpty(string))
            return null;
        return UUID.fromString(string);
    }

}
