package com.example.gestionbassins.restcontrollers;

import com.example.gestionbassins.entities.Avis;
import com.example.gestionbassins.service.AvisService;
import com.example.gestionbassins.service.AvisServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//Add this import at the top of the file
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/avis")
@CrossOrigin(origins = "http://localhost:4200")
public class AvisRestController {

    @Autowired
    private AvisService avisService;
    private static final Logger logger = LoggerFactory.getLogger(AvisServiceImpl.class);

    @GetMapping("/bassin/{idBassin}")
    public List<Avis> getAvisByBassinId(@PathVariable Long idBassin) {
        return avisService.getAvisByBassinId(idBassin);
    }

    @PostMapping("/add/{idBassin}")
    public ResponseEntity<Avis> addAvis(@PathVariable Long idBassin, @RequestBody Avis avis) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = avis.getNom(); // Utilisez le nom fourni dans la requête
        boolean isAuthenticated = false;

        // Si l'utilisateur est connecté, utilisez son nom d'utilisateur à la place
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
            isAuthenticated = true;
        }

        // Si le nom est vide et l'utilisateur n'est pas authentifié, renvoyer une erreur
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Avis newAvis = avisService.addAvis(avis, idBassin, username, isAuthenticated);
        return ResponseEntity.ok(newAvis);
    }

    @PutMapping("/update/{idAvis}")
    public ResponseEntity<Avis> updateAvis(@PathVariable Long idAvis, @RequestBody Avis updatedAvis) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        Avis updated = avisService.updateAvis(idAvis, updatedAvis, username);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/delete/{idAvis}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteAvis(@PathVariable Long idAvis) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            logger.info("Delete request for avis ID: {} by user: {}", idAvis, username);
            avisService.deleteAvis(idAvis, username);
            return ResponseEntity.ok().body(Map.of("message", "Avis supprimé avec succès."));
        } catch (RuntimeException e) {
            logger.error("Error in deleteAvis: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error in deleteAvis: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Une erreur est survenue lors de la suppression"));
        }
    }
    
    @GetMapping("/all")
    public List<Avis> getAllAvis() {
        return avisService.getAllAvis();
    }
}