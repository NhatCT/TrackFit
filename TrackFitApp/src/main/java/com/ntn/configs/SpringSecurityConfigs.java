/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ntn.filters.JwtFilter;
import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.ntn.controllers",
    "com.ntn.repositories",
    "com.ntn.services"
})
public class SpringSecurityConfigs {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable())
         .authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/login", "/api/register").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/secure/exercises/**").hasAnyRole("USER","ADMIN")
    .requestMatchers("/api/secure/exercises/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    .requestMatchers("/").permitAll()
    .anyRequest().authenticated()
)

          // Nếu bạn dùng form login cho web (không chỉ API/JWT), giữ lại:
          .formLogin(form -> form
              .loginPage("/login").loginProcessingUrl("/login")
              .defaultSuccessUrl("/", true)
              .failureUrl("/login?error=true").permitAll()
          )
          .logout(logout -> logout.logoutSuccessUrl("/login").permitAll());

        // JWT filter (đặt trước UsernamePasswordAuthenticationFilter)
        http.addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public Cloudinary cloudinary() {
        // Khuyến nghị: lấy key từ ENV thay vì hard-code.
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dywix6n0z",
            "api_key",    "198396299352167",
            "api_secret", "Hlh12SuOkmrk7ZRQTX8f-nkDwTY",
            "secure", true
        ));
    }

    @Bean
    @Order(0)
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Không có dấu / ở cuối, và bật credentials
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
