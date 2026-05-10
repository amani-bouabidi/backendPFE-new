package com.ira.formation.security;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 
import java.util.List;
 
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
 
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
 
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                         UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }
 
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
 
                // ================= PUBLIC =================
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/domaines/public").permitAll()
                .requestMatchers("/api/formations/public").permitAll()
                .requestMatchers("/api/formations/domaine/**").permitAll()
                .requestMatchers("/api/domaines/catalogue").permitAll()
 
                // ================= ADMIN =================
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/domaines/admin").hasRole("ADMIN")
                .requestMatchers("/api/formations/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/modules/**").hasRole("ADMIN")
                .requestMatchers("/api/attestations/generate").hasRole("ADMIN")
                .requestMatchers("/api/attestations/all").hasRole("ADMIN")
 
                // ================= FORMATEUR =================
                .requestMatchers("/api/formations/formateur").hasRole("FORMATEUR")
                .requestMatchers("/api/questions/**").hasRole("FORMATEUR")
                .requestMatchers("/api/choix/**").hasRole("FORMATEUR")
                .requestMatchers("/api/sessions/creer").hasRole("FORMATEUR")
                .requestMatchers("/api/sessions/formateur").hasRole("FORMATEUR")
                .requestMatchers("/api/sessions/mettre-a-jour/**").hasRole("FORMATEUR")
                .requestMatchers("/api/sessions/supprimer/**").hasRole("FORMATEUR")
                .requestMatchers("/api/sessions/terminer/**").hasRole("FORMATEUR")
                .requestMatchers("/api/progress/formation/**").hasRole("FORMATEUR")
 
                // ✅ FIX: Tests — règles spécifiques AVANT la règle générale
                // L'apprenant doit pouvoir accéder au test de sa formation
                .requestMatchers("/api/tests/formation/*/apprenant").hasRole("APPRENANT")
                .requestMatchers("/api/tests/pass/**").hasRole("APPRENANT")
                // Le formateur gère les tests (créer, modifier, supprimer)
                .requestMatchers("/api/tests/**").hasAnyRole("FORMATEUR", "ADMIN")
 
                // ================= SESSIONS PARTAGÉES =================
                .requestMatchers("/api/sessions/apprenant/**").hasRole("APPRENANT")
                .requestMatchers("/api/sessions/**").hasAnyRole("FORMATEUR", "APPRENANT")
 
                // ================= APPRENANT =================
                .requestMatchers("/api/formations/apprenant").hasRole("APPRENANT")
                .requestMatchers("/api/notifications/**").hasAnyRole("APPRENANT", "FORMATEUR", "ADMIN")
                .requestMatchers("/api/progress/my").hasRole("APPRENANT")
                .requestMatchers("/api/progress/resume").hasRole("APPRENANT")
                .requestMatchers("/api/progress/complete").hasRole("APPRENANT")
                .requestMatchers("/api/attestations/my").hasRole("APPRENANT")
 
                // ================= PROGRESS ADMIN =================
                .requestMatchers("/api/progress/admin/**").hasRole("ADMIN")
 
                // ================= SHARED =================
                .requestMatchers("/api/documents/download/**")
                    .hasAnyRole("ADMIN", "FORMATEUR", "APPRENANT")
                .requestMatchers("/api/documents/upload/**").hasRole("ADMIN")
                .requestMatchers("/api/videos/download/**")
                    .hasAnyRole("ADMIN", "FORMATEUR", "APPRENANT")
                .requestMatchers("/api/videos/stream/**")
                    .hasAnyRole("ADMIN", "FORMATEUR", "APPRENANT")
                .requestMatchers("/api/videos/upload/**").hasRole("ADMIN")
                .requestMatchers("/api/favorites/**")
                    .hasAnyRole("ADMIN", "FORMATEUR", "APPRENANT")
                .requestMatchers("/api/events/**")
                    .hasAnyRole("FORMATEUR", "APPRENANT")
                .requestMatchers("/api/attestations/download/**")
                    .hasAnyRole("ADMIN", "FORMATEUR", "APPRENANT")
 
                // ================= FALLBACK =================
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
 
        return http.build();
    }
 
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
 
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
 
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
 
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}