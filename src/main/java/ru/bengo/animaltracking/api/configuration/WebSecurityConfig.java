package ru.bengo.animaltracking.api.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.bengo.animaltracking.api.configuration.filter.UserAuthenticatedFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserAuthenticatedFilter userAuthenticatedFilter;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(userAuthenticatedFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/registration"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/error"))
                        .permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/accounts/**",
                                "/animals/**",
                                "/animals/types/**",
                                "/locations/**")
                        .permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
