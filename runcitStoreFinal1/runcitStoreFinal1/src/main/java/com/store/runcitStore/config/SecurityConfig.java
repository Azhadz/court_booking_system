package com.store.runcitStore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests().anyRequest().permitAll().and().csrf().disable();
        return http.build();
    }

    /**@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests()
                .requestMatchers("/register", "/login", "/error_page", "/css/**", "/js/**").permitAll() // Allow access to public pages
                .anyRequest().authenticated() // All other requests require authentication
            .and()
            .formLogin()
                .usernameParameter("login") // Maps to your `login` field
                .passwordParameter("password")
                .loginPage("/login") // Custom login page
                .defaultSuccessUrl("/products/index", true) // Redirect on successful login
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            .and()
            .csrf().disable(); // Disable CSRF for simplicity (enable in production with proper setup)
        return http.build();
    }**/

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
