package com.example.gestionbassins.restcontrollers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class ImagePersonnaliseRestController {

	private final Path imageStorageLocation = Paths.get("C:/shared/imagesaccessoiresbassin").toAbsolutePath().normalize();

    @GetMapping("/imagespersonnalise/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable("filename") String filename) {
        try {
            Path filePath = imageStorageLocation.resolve(filename).normalize();
            System.out.println("Chemin du fichier : " + filePath); // Log pour débogage

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG) // ou MediaType.IMAGE_PNG selon le type d'image
                        .body(resource);
            } else {
                System.out.println("Fichier non trouvé : " + filePath); // Log pour débogage
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du fichier : " + e.getMessage()); // Log pour débogage
            return ResponseEntity.internalServerError().build();
        }
    }
    
    
    
}