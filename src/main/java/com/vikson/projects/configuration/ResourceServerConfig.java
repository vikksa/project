package com.vikson.projects.configuration;

import com.vikson.oauth2.viksonResourceServerAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

@Configuration
public class ResourceServerConfig  extends viksonResourceServerAdapter {

    @Override
    protected void configurePaths(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry) {
        registry
            .antMatchers("/api/internal/v2/projects/**").permitAll()
            .antMatchers("/api/**").authenticated();

    }
}
