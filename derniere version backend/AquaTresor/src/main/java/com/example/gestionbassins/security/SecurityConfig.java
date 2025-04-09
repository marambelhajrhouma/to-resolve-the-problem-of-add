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
	        .addFilterBefore(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
	        .authorizeHttpRequests(auth -> auth
	            
	        	// Gestion des bassins
	            .requestMatchers("/api/addbassin").hasAuthority("ADMIN")
	            .requestMatchers("/api/addBassinWithImages/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/all").permitAll()
	            .requestMatchers("/api/getbyid").permitAll()
	            .requestMatchers("/api/imagesBassin/**").permitAll()
	            .requestMatchers("/api/updateBassinWithImg/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/updatebassin/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/deletebassin/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/image/**").permitAll()
	            .requestMatchers("/api/categories/**").permitAll()

	            //bassins personnaliser 
	            .requestMatchers("/api/bassinpersonnalise/ajouterBassinPersonnalise/**").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/getAllBassinPersonnalise").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/detailBassinPersonnalise/**").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/supprimerBassinPersonnalise/**").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/mettreAJourBassinPersonnalise/**").permitAll()
	            .requestMatchers("/api/imagespersonnalise/**").permitAll()
	            .requestMatchers("/api/bassinpersonnalise/options/**").permitAll()
	            	            

	            // Gestion des avis
	            .requestMatchers("/api/avis/all").permitAll() // Tout le monde peut voir les avis
	            .requestMatchers("/api/avis/bassin/**").permitAll() // Avis par bassin accessible à tous
	            .requestMatchers("/api/avis/add/**").authenticated() // Seuls les utilisateurs authentifiés peuvent ajouter des avis
	            .requestMatchers(HttpMethod.PUT, "/api/avis/update/**").authenticated() // Authentification requise pour modifier
	            .requestMatchers(HttpMethod.DELETE, "/api/avis/delete/**").authenticated()
	            .requestMatchers("/api/avis/user/**").authenticated() // Seuls les utilisateurs authentifiés peuvent voir leurs avis
	            
	            // Gestion des promotions
	            .requestMatchers("/api/promotions/add").hasAuthority("ADMIN")
	            .requestMatchers("/api/promotions/bassins").permitAll()
		           
	            .requestMatchers("/api/promotions/update/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/promotions/applyToBassins/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/promotions/applyToCategorie/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/promotions/all").permitAll()
	            .requestMatchers("/api/promotions/**").permitAll()
	            .requestMatchers("/api/promotions/delete/**").hasAuthority("ADMIN")
	            
	            
	            // Gestion des stocks
	            .requestMatchers("/api/{id}/archiver").hasAuthority("ADMIN")
	            .requestMatchers("/api/{id}/desarchiver").hasAuthority("ADMIN")
	            .requestMatchers("/api/{id}/mettre-a-jour-quantite").hasAuthority("ADMIN")
	            .requestMatchers("/api/non-archives*").hasAuthority("ADMIN")
	            .requestMatchers("/api/archives").hasAuthority("ADMIN")
	            .requestMatchers("/api/transactions").hasAuthority("ADMIN")
	            .requestMatchers("/api/notifier-stock-faible").hasAuthority("ADMIN")
	           
	            //Notifications
	            
	            .requestMatchers("/api/notifications").hasAuthority("ADMIN")
	            .requestMatchers("/api/notifications/**").hasAuthority("ADMIN")
	            .requestMatchers("/api/notifications/read-all ").hasAuthority("ADMIN")
	            .requestMatchers("/api/notifications/{id}/read").hasAuthority("ADMIN")
	               
	            .anyRequest().permitAll()
	        );

	    return http.build();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration cors = new CorsConfiguration();
	    cors.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // Allow Angular app
	    cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow all necessary methods
	    cors.setAllowCredentials(true); // Allow credentials (e.g., cookies)
	    cors.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")); // Allow necessary headers
	    cors.setExposedHeaders(Collections.singletonList("Authorization")); // Expose Authorization header
	    cors.setMaxAge(3600L); // Cache preflight response for 1 hour

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", cors); // Apply CORS to all endpoints
	    return source;
	}
}