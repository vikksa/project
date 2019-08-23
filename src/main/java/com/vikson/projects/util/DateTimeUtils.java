package com.vikson.projects.util;

import com.vikson.projects.exceptions.ApiExceptionHelper;
import com.vikson.projects.exceptions.ErrorCodes;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static ZonedDateTime safelyParse(String input) {
        if (StringUtils.isEmpty(input)) {
            return null;
        }
        try {
            return ZonedDateTime.parse(input, dtf);
        } catch (DateTimeParseException e) {
            throw ApiExceptionHelper.newBadRequestError(ErrorCodes.BAD_TIMESTAMP_FORMAT,StringUtils.quote(input), e);
        }
    }

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

}
