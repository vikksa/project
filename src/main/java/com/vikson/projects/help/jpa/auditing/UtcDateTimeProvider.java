package com.vikson.projects.help.jpa.auditing;

import org.springframework.data.auditing.DateTimeProvider;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

public class UtcDateTimeProvider implements DateTimeProvider {

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(ZonedDateTime.now(ZoneId.of("UTC")));
    }

}
