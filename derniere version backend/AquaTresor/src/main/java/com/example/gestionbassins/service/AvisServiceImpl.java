package com.example.gestionbassins.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.gestionbassins.entities.Avis;
import com.example.gestionbassins.entities.Bassin;
import com.example.gestionbassins.entities.User;
import com.example.gestionbassins.repos.AvisRepository;
import com.example.gestionbassins.repos.BassinRepository;
import com.example.gestionbassins.security.SecParams;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;
//Add this import at the top of the file
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AvisServiceImpl implements AvisService {

    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private BassinRepository bassinRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String USER_SERVICE_URL = "http://localhost:8002/users";

    private static final Logger logger = LoggerFactory.getLogger(AvisServiceImpl.class);

    /*
    @Override
    public Avis addAvis(Avis avis, Long idBassin, String username, boolean isAuthenticated) {
        // Vérifier l'existence du bassin
        Bassin bassin = bassinRepository.findById(idBassin)
            .orElseThrow(() -> new RuntimeException("Bassin non trouvé"));

        // Si l'utilisateur est authentifié, récupérer ses informations
        if (isAuthenticated) {
            // Récupérer le JWT
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String jwt = getJwtFromAuthentication(authentication);

            // Créer les en-têtes avec le JWT
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);

            // Créer une requête HTTP avec les en-têtes
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Appeler users-microservice pour récupérer les informations de l'utilisateur
            ResponseEntity<User> userResponse = restTemplate.exchange(
                USER_SERVICE_URL + "/username/" + username,
                HttpMethod.GET,
                entity,
                User.class
            );

            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Utilisateur non trouvé ou non valide");
            }

            // Récupérer les informations de l'utilisateur
            User user = userResponse.getBody();
            avis.setUserId(user.getUser_id()); // Utiliser l'ID de l'utilisateur
            avis.setNom(username); // Définir le nom avec le nom d'utilisateur
        } else {
            avis.setUserId(null); // Aucun ID utilisateur pour les avis non authentifiés
            // Laisser le nom tel quel (celui fourni par l'utilisateur)
        }

        // Associer l'avis au bassin
        avis.setBassin(bassin);

        // Enregistrer l'avis
        return avisRepository.save(avis);
    }
    */
    
    private String getJwtFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Aucune authentification trouvée");
        }

        // Vérifier si l'authentification contient un JWT
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            Object credentials = authentication.getCredentials();
            if (credentials instanceof String) {
                return (String) credentials;
            }
        }

        // Si le JWT n'est pas trouvé dans les credentials, essayer de le récupérer depuis l'en-tête
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String jwt = request.getHeader("Authorization");

            if (jwt == null || !jwt.startsWith(SecParams.PREFIX)) {
                throw new RuntimeException("JWT non trouvé dans l'authentification");
            }

            return jwt.substring(SecParams.PREFIX.length()); // Retirer le préfixe "Bearer "
        } catch (Exception e) {
            throw new RuntimeException("Impossible de récupérer le JWT: " + e.getMessage());
        }
    }
    @Override
    public List<Avis> getAvisByBassinId(Long idBassin) {
        // Récupérer tous les avis associés à un bassin spécifique
        return avisRepository.findByBassinIdBassin(idBassin);
    }

    
    @Override
    public Avis addAvis(Avis avis, Long idBassin, String username, boolean isAuthenticated) {
        Bassin bassin = bassinRepository.findById(idBassin)
            .orElseThrow(() -> new RuntimeException("Bassin non trouvé"));

        if (isAuthenticated) {
            // Récupérer l'utilisateur connecté
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String jwt = getJwtFromAuthentication(authentication);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwt);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<User> userResponse = restTemplate.exchange(
                USER_SERVICE_URL + "/username/" + username,
                HttpMethod.GET,
                entity,
                User.class
            );

            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Utilisateur non trouvé ou non valide");
            }

            User user = userResponse.getBody();
            avis.setUserId(user.getUser_id());
            avis.setNom(username);
        } else {
            avis.setUserId(null);
        }

        avis.setBassin(bassin);
        avis.setDateSoumission(new Date()); // Date de soumission
        return avisRepository.save(avis);
    }

    @Override
    public Avis updateAvis(Long idAvis, Avis updatedAvis, String username) {
        System.out.println("Tentative de mise à jour de l'avis ID: " + idAvis);
        Avis existingAvis = avisRepository.findById(idAvis)
            .orElseThrow(() -> {
                System.out.println("Avis non trouvé avec ID: " + idAvis);
                return new RuntimeException("Avis non trouvé");
            });

        System.out.println("Utilisateur actuel: " + username);
        System.out.println("Auteur de l'avis: " + existingAvis.getNom());

        if (!existingAvis.getNom().equals(username)) {
            System.out.println("Utilisateur non autorisé à modifier cet avis");
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cet avis");
        }

        // Sauvegarder une copie de l'ancien message
     //   existingAvis.setAncienMessage(existingAvis.getMessage());
        // Ajouter l'ancien message à l'historique
        existingAvis.addHistoriqueMessage(existingAvis.getMessage());

        // Mettre à jour l'avis
        existingAvis.setMessage(updatedAvis.getMessage());
        existingAvis.setNote(updatedAvis.getNote());
        existingAvis.setDateModification(new Date()); // Date de modification

        System.out.println("Mise à jour de l'avis avec succès");
        return avisRepository.save(existingAvis);
    }
    
    @Override
    public void deleteAvis(Long idAvis, String username) {
        logger.info("Attempting to delete avis with ID: {} by user: {}", idAvis, username);
        
        // Check if avis exists first, using proper null handling
        if (!avisRepository.existsById(idAvis)) {
            logger.error("Avis with ID: {} not found", idAvis);
            throw new RuntimeException("Avis non trouvé");
        }
        
        Avis avis = avisRepository.findById(idAvis).get(); // Safe now as we checked existence
        
        // Check if current user is the author of the review or an admin
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        
        logger.debug("User: {}, Is admin: {}, Avis author: {}", username, isAdmin, avis.getNom());
        
        // Allow deletion if user is the author or an admin
        if (!avis.getNom().equals(username) && !isAdmin) {
            logger.warn("Unauthorized deletion attempt by user: {} for avis ID: {}", username, idAvis);
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cet avis");
        }
        
        try {
            // Delete the review
            avisRepository.delete(avis);
            logger.info("Successfully deleted avis with ID: {}", idAvis);
        } catch (Exception e) {
            logger.error("Error deleting avis with ID: {}: {}", idAvis, e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de l'avis: " + e.getMessage());
        }
    }
    
    public List<Avis> getAllAvis() {
        return avisRepository.findAll();
    }
}