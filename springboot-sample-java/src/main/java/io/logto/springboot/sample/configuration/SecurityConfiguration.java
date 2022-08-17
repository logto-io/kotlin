package io.logto.springboot.sample.configuration;

import io.logto.springboot.sample.handler.LogoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    @Autowired
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LogoutHandler logoutHandler) throws Exception {
        http.authorizeRequests()
                // Anyone can access the home page.
                .mvcMatchers("/").permitAll()
                // Other pages/requests should be authenticated.
                .anyRequest().authenticated()
                .and().oauth2Login()
                // Use `/sign-out` path for logout requests.
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/sign-out"))
                .addLogoutHandler(logoutHandler);
        return http.build();
    }
}
