package com.example.gestionbassins.restcontrollers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gestionbassins.dto.BassinDTO;
import com.example.gestionbassins.entities.Bassin;
import com.example.gestionbassins.entities.ImageBassin;
import com.example.gestionbassins.repos.BassinRepository;
import com.example.gestionbassins.repos.ImageBassinRepository;
import com.example.gestionbassins.service.BassinService;
import com.example.gestionbassins.service.ImageBassinService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class BassinRestController {

	private final String uploadDir = "C:/shared/images/"; 
	
    @Autowired
    BassinService bassinService; 
    
    @Autowired
    ImageBassinService imageBassinService;
    
    @Autowired
    ImageBassinRepository imageBassinRepository;
    
    @Autowired
    BassinRepository bassinRepository;

    // Get all events
   @RequestMapping(path="all", method=RequestMethod.GET)
    public List<Bassin> getAllBassins() {
        return bassinService.getAllBassins();
    }
    
   
    @RequestMapping(value="getbyid/{idBassin}", method=RequestMethod.GET)
    public Bassin getBassinById(@PathVariable("idBassin") Long id) {
        return bassinService.getBassin(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/addbassin")
    public Bassin createBassin(@RequestBody Bassin bassin) {
   	 System.out.println(bassin);
       System.out.println("Received prixBassin: " + bassin.getPrix()); // Debugging
       return bassinService.saveBassin(bassin);
   }
    
    @PostMapping("/addBassinWithImages") 
    public ResponseEntity<?> addBassinWithImages(
            @RequestParam("bassin") String bassinJson,
            @RequestParam("images") List<MultipartFile> images) {
        try {
            // 1. Convertir le JSON en objet Bassin
            ObjectMapper mapper = new ObjectMapper();
            Bassin bassin = mapper.readValue(bassinJson, Bassin.class);

            // 2. Vérifier si l'ID existe (Mise à jour)
            if (bassin.getIdBassin() != null) {
                bassin = bassinService.getBassin(bassin.getIdBassin());
                if (bassin == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Bassin introuvable avec ID : " + bassin.getIdBassin());
                }
            } else {
                // 3. Si l'ID est null, enregistrer le bassin
                bassin = bassinService.saveBassin(bassin);
            }

            // 4. Ajouter les images associées
            if (images != null && !images.isEmpty()) {
                imageBassinService.uploadImages(bassin, images.toArray(new MultipartFile[0]));
            }

            // 🔥 Recharger le bassin pour inclure les images
            Bassin updatedBassin = bassinService.getBassin(bassin.getIdBassin());

            return ResponseEntity.ok(updatedBassin);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'ajout du bassin et des images : " + e.getMessage());
        }
    }


    
    @PutMapping("/updatebassin/{idBassin}")
    public Bassin updateBassin(@PathVariable("idBassin") Long idBassin, @RequestBody Bassin bassin) {
    	bassin.setIdBassin(idBassin); // Set the ID to ensure the correct event is updated
        return bassinService.updateBassin(bassin);
    }
    
    @PostMapping("/updateBassinWithImg")
    public Bassin updateBassinWithImg(@RequestPart("bassin") String bassinJson,
                                      @RequestPart(value = "files", required = false) MultipartFile[] files) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Bassin b = objectMapper.readValue(bassinJson, Bassin.class);

        // Récupérer le bassin existant depuis la base de données
        Bassin existingBassin = bassinRepository.findByIdWithImages(b.getIdBassin())
                .orElseThrow(() -> new RuntimeException("Bassin non trouvé avec l'ID : " + b.getIdBassin()));

        // Mise à jour des propriétés du bassin
        existingBassin.setNomBassin(b.getNomBassin());
        existingBassin.setDescription(b.getDescription());
        existingBassin.setPrix(b.getPrix());
        existingBassin.setMateriau(b.getMateriau());
        existingBassin.setCouleur(b.getCouleur());
        existingBassin.setDimensions(b.getDimensions());
        existingBassin.setDisponible(b.isDisponible());
        existingBassin.setStock(b.getStock());
        existingBassin.setCategorie(b.getCategorie());

        // Traitement des fichiers images
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();

                    // Extraire l'index de l'image à partir du nom du fichier (format : "id_index.extension")
                    String[] filenameParts = originalFilename.split("_");
                    if (filenameParts.length > 1) {
                        try {
                            int imageIndex = Integer.parseInt(filenameParts[1].split("\\.")[0]) - 1; // Index commence à 0

                            // Vérifier que l'index est valide
                            if (imageIndex >= 0 && imageIndex < existingBassin.getImagesBassin().size()) {
                                ImageBassin oldImage = existingBassin.getImagesBassin().get(imageIndex);

                                // Supprimer l'ancienne image du dossier
                                Path oldImagePath = Paths.get(uploadDir + oldImage.getName());
                                Files.deleteIfExists(oldImagePath);

                                // Générer un nouveau nom de fichier
                                String extension = FilenameUtils.getExtension(originalFilename);
                                String newImageName = b.getIdBassin() + "_" + (imageIndex + 1) + "." + extension;

                                // Sauvegarder la nouvelle image dans le dossier
                                Path newImagePath = Paths.get(uploadDir + newImageName);
                                Files.copy(file.getInputStream(), newImagePath, StandardCopyOption.REPLACE_EXISTING);

                                // Créer un nouvel objet ImageBassin
                                ImageBassin newImage = new ImageBassin();
                                newImage.setName(newImageName); // Nom du fichier
                                newImage.setType(file.getContentType());
                                newImage.setImage(file.getBytes());
                                newImage.setBassin(existingBassin);

                                // Définir imagePath avec uniquement le nom du fichier
                                newImage.setImagePath(newImageName);

                                // Remplacer l'ancienne image par la nouvelle
                                existingBassin.getImagesBassin().set(imageIndex, newImage);
                            }
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Format de nom de fichier invalide : " + originalFilename);
                        } catch (IOException e) {
                            throw new RuntimeException("Erreur lors de l'enregistrement de l'image : " + e.getMessage());
                        }
                    }
                }
            }
        }

        // Sauvegarder et retourner le bassin mis à jour
        return bassinRepository.save(existingBassin);
    }









    @DeleteMapping("deletebassin/{idBassin}")
    public ResponseEntity<?> deleteBassin(@PathVariable("idBassin") Long idBassin) {
        try {
            bassinService.deleteBassinById(idBassin);
            return ResponseEntity.ok().build(); // Return 200 OK on success
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error deleting bassin: " + e.getMessage());
        }
    }
    
    //Affiche la liste des bassin appartient à une catégorie 
    @RequestMapping(value="/Categories/{idCategorie}", method=RequestMethod.GET)
    public List<Bassin> getBassinByCategorieId(@PathVariable("idCategorie") Long idCategorie) {
        return bassinService.findByCategorieIdCategorie(idCategorie);
    }
    
    @RequestMapping(value="/bassinByName/{nom}",method = RequestMethod.GET)
    public List<Bassin> findByNomBassinContains(@PathVariable("nom") String nom) {
    	return bassinService.findByNomBassinContains(nom);
    }
    
}

