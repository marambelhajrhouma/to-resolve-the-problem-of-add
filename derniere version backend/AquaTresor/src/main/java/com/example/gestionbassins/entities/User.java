package com.example.gestionbassins.entities;

import lombok.Data;

@Data
public class User {
    private Long user_id;
    private String username;
    private String password;
    private Boolean enabled;
    private String email;
    private String profileImage;
    private String resetToken;
    private String validationCode;
    private String jwtToken;
    
    //La classe User est nécessaire pour
    // désérialiser la réponse du microservice 
    // users-microservice. Vous devez créer 
    //cette classe dans le microservice aquatresor.
    
}