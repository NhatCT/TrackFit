package com.ntn.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ntn.filters.JwtFilter;

import java.util.List;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // 👈 thêm
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
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 👈 thêm
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.ntn.controllers",
    "com.ntn.repositories",
    "com.ntn.services",
    "com.ntn.notify",      // 👈 nếu có publisher
    "com.ntn.configs"      // 👈 đảm bảo quét WebSocketConfig
})
public class SpringSecurityConfigs {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter();
    }

    // ===== Chain 0: WebSocket handshake =====
    @Bean
    @Order(0)
    public SecurityFilterChain wsSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/ws/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // bỏ CSRF cho WS handshake
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ws/**").permitAll()
                .anyRequest().denyAll()
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    // ===== Chain 1: API (/api/**) - JWT, stateless =====
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public API
                .requestMatchers("/api/login", "/api/register").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Notifications (admin gửi thay mặt)
                .requestMatchers("/api/secure/notifications/admin/**").hasRole("ADMIN")
                // Notifications (self): xem/tạo/list/đánh dấu... -> cần đăng nhập
                .requestMatchers("/api/secure/notifications/**").authenticated()

                // Exercises API: GET cho USER/ADMIN; tạo/sửa/xóa chỉ ADMIN
                .requestMatchers(HttpMethod.GET, "/api/secure/exercises/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/secure/exercises/**").hasRole("ADMIN")

                // Các API secure khác
                .requestMatchers("/api/secure/**").authenticated()

                // Mặc định
                .anyRequest().denyAll()
            );

        // JWT filter chỉ áp dụng cho /api/**
        http.addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ===== Chain 2: MVC (các route còn lại) - session + formLogin =====
    @Bean
    @Order(2)
    public SecurityFilterChain mvcSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login",
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/vendor/**")
                    .permitAll()
                .requestMatchers("/dashboard/**", "/users/**", "/plans/**", "/stats/**")
                    .hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login").loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true").permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/login").permitAll());

        return http.build();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public Cloudinary cloudinary() {
        // FIXME: Lấy từ ENV thay vì hard-code
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dywix6n0z",
                "api_key", "198396299352167",
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
        // KHÔNG có "/" cuối, thêm domain FE của bạn
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
