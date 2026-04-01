package com.zjb.mjgl.config;


import com.zjb.mjgl.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(CsrfConfigurer::disable) // 基于token，不需要csrf
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 基于token，不需要session
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(new AntPathRequestMatcher("/getPicCheckCode", POST.name())).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login/", GET.name())).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/signup")).permitAll()
                        // WebSocket 握手需要放行，否则连接会被 401 拦截
                        .requestMatchers(new AntPathRequestMatcher("/api/ws/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/ws/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
