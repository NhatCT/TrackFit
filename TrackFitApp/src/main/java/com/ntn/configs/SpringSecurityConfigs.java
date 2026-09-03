package com.ntn.configs;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ntn.filters.JwtFilter;
import com.ntn.filters.RateLimitFilter;
import com.ntn.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.ntn.controllers",
    "com.ntn.repositories",
    "com.ntn.services",
    "com.ntn.configs",
    "com.ntn.messaging"
})
public class SpringSecurityConfigs {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${allowed.origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String allowedOriginsProp;

    @Value("${rate-limit.trust-proxy:false}")
    private boolean rateLimitTrustProxy;

    @Value("${rate-limit.limit:60}")
    private int rateLimitGeneral;

    @Value("${rate-limit.auth-limit:10}")
    private int rateLimitAuth;

    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;

    @Value("${cloudinary.api-key:}")
    private String cloudinaryApiKey;

    @Value("${cloudinary.api-secret:}")
    private String cloudinaryApiSecret;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtFilter jwtFilter() {
        JwtUtils.validateConfiguration();
        return new JwtFilter();
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter(redisConnectionFactory, rateLimitTrustProxy,
                rateLimitGeneral, rateLimitAuth);
    }

    @Bean
    @Order(0)
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(c -> c.configurationSource(cors()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login", "/api/register", "/api/login/google").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // admin API
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // secure API (USER hoặc ADMIN)
                .requestMatchers("/api/secure/**").authenticated()
                .anyRequest().denyAll()
                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter(), JwtFilter.class);

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain mvc(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                // CSRF stays ENABLED (Spring default) on this session/form-login MVC chain.
                // Thymeleaf auto-injects the CsrfToken into forms rendered with th:action.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login",
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/vendor/**", "/ws/**").permitAll()
                .requestMatchers("/dashboard/**", "/users/**", "/plans/**", "/stats/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                )
                .formLogin(f -> f.loginPage("/login").loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true).failureUrl("/login?error=true").permitAll())
                .logout(l -> l.logoutSuccessUrl("/login").permitAll());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource cors() {
        var cfg = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOriginsProp.split(","))
                .map(String::trim)
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .filter(s -> !s.isEmpty())
                .toList();
        // Use EXACT origins (not patterns): credentials are allowed, so broad
        // *.vercel.app / *.onrender.com wildcards would be an open door. Operators
        // must set ALLOWED_ORIGINS to their real frontend URL(s).
        cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    // optional beans
    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    @Order(0)
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryCloudName,
                "api_key", cloudinaryApiKey,
                "api_secret", cloudinaryApiSecret,
                "secure", true
        ));
    }
}
