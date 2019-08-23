package com.vikson.projects.help.jpa.auditing;

import com.vikson.services.users.PersonalApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<UUID> {

    @Autowired
    private PersonalApiClient personalApiClient;

    @Override
    public Optional<UUID> getCurrentAuditor() {
        if(isAuthenticated()) {
            return Optional.of(personalApiClient.getMe().getUserId());
        } else {
            return Optional.empty();
        }
    }

    private boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                //when Anonymous Authentication is enabled
                !(SecurityContextHolder.getContext().getAuthentication()
                        instanceof AnonymousAuthenticationToken);
    }

}
