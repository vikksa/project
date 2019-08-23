package com.vikson.projects.configuration;

import com.vikson.projects.help.jpa.auditing.UtcDateTimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider", auditorAwareRef = "springSecurityAuditorAware")
public class AuditingConfiguration {

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return new UtcDateTimeProvider();
    }

}
