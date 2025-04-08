package com.example.gestionbassins.security;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.gestionbassins.security.JWTAuthorizationFilter;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled= true)
public class SecurityConfig {
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	        .addFilterBefore(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class) // Add JWT filter
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/api/addbassin").hasAuthority("ADMIN") 
	            .requestMatchers("/api/addBassinWithImages/**").permitAll()
	            .requestMatchers("/api/all").hasAnyAuthority("ADMIN", "USER")
	            .requestMatchers("/api/getbyid").hasAnyAuthority("ADMIN", "USER")
	            
	            .requestMatchers("/api/imagesBassin/**").permitAll()
	            
	            .requestMatchers("/api/updateBassinWithImg/**").permitAll() 
	            .requestMatchers("/api/updatebassin/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/deletebassin/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/image/**").permitAll()
	            .requestMatchers("/api/categories/**").hasAnyAuthority("ADMIN", "USER")
	    		
	            .requestMatchers("/api/bassinpersonnalise/ajouterBassinPersonnalise/**").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/getAllBassinPersonnalise").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/detailBassinPersonnalise/**").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/supprimerBassinPersonnalise/**").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/mettreAJourBassinPersonnalise/**").permitAll()
	            .requestMatchers("/api/imagespersonnalise/**").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/options/**").permitAll()
	            
	            .anyRequest().permitAll() 
	        );

	    return http.build();
	}


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:2400")); // ✅ Autoriser plusieurs origines
        cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowCredentials(true);
        cors.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        cors.setExposedHeaders(Collections.singletonList("Authorization"));
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
	
  

}











	


