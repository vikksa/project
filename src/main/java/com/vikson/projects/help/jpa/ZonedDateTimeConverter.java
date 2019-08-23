package com.vikson.projects.help.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Converts {@link ZonedDateTime} to {@link Timestamp}s and back. Will always return {@link ZonedDateTime} at UTC
 * timezone.
 *
 * @since 1.0
 * @author Vikram
 */
@Converter(autoApply = true)
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {
    @Override
    public Timestamp convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
        return zonedDateTime != null? Timestamp.from(zonedDateTime.toInstant()) : null;
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(Timestamp timestamp) {
        return timestamp != null? timestamp.toInstant().atZone(ZoneId.of("Z")) : null;
    }
}

