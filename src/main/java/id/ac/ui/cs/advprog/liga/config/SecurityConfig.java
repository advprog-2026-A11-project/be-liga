package id.ac.ui.cs.advprog.liga.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
  
  // NEW: Grab the URL from application.properties
  @Value("${frontend.url}")
  private String frontendUrl;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .cors(cors -> cors.configurationSource(corsConfigurationSource()))
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(auth -> auth
              .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
              // Public endpoints
              .requestMatchers("/api/clan/list", "/api/clan/detail/**").permitAll()
              // Internal endpoint called by be-bacaan — permit without user JWT
              .requestMatchers("/api/internal/score-update").permitAll()
              // Current season info is public
              .requestMatchers("/api/league/current-season").permitAll()
              // Everything else needs authentication
              .anyRequest().authenticated()
          )
          .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
      return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration configuration = new CorsConfiguration();
      
      // NEW: Use the dynamic URL, and support multiple URLs if separated by commas
      configuration.setAllowedOrigins(Arrays.asList(frontendUrl.split(","))); 
      
      configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      configuration.setAllowedHeaders(List.of("*"));
      configuration.setAllowCredentials(true);
      
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
  }
}