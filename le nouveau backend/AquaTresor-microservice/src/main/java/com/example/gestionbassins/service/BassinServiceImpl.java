package com.example.gestionbassins.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.gestionbassins.dto.BassinDTO;
//import com.example.gestionbassins.dto.ImageDTO;
import com.example.gestionbassins.entities.Bassin;
import com.example.gestionbassins.entities.Categorie;
//import com.example.gestionbassins.entities.Image;
import com.example.gestionbassins.entities.ImageBassin;
import com.example.gestionbassins.repos.BassinRepository;
import com.example.gestionbassins.repos.ImageBassinRepository;
//import com.example.gestionbassins.repos.ImageRepository;



@Service
public class BassinServiceImpl implements BassinService{

	@Autowired
	BassinRepository bassinRepository;
		
	@Autowired
	ImageBassinService imageBassinService;
	
	@Autowired
	ImageBassinRepository imageBassinRepository;
	
	//@PreAuthorize("hasAuthority('ADMIN')")
	@Override
	public Bassin saveBassin(Bassin b) {
		
		return bassinRepository.save(b) ;
	}
	
	@Override
	public Bassin updateBassin(Bassin b) { 
	    // Récupérer le bassin existant
	    Bassin existingBassin = bassinRepository.findByIdWithImages(b.getIdBassin())
	        .orElseThrow(() -> new RuntimeException("Bassin non trouvé avec l'ID : " + b.getIdBassin()));

	    // Mettre à jour les propriétés du bassin
	    existingBassin.setNomBassin(b.getNomBassin());
	    existingBassin.setDescription(b.getDescription());
	    existingBassin.setPrix(b.getPrix());
	    existingBassin.setMateriau(b.getMateriau());
	    existingBassin.setCouleur(b.getCouleur());
	    existingBassin.setDimensions(b.getDimensions());
	    existingBassin.setDisponible(b.isDisponible());
	    existingBassin.setStock(b.getStock());
	    existingBassin.setCategorie(b.getCategorie());

	    // Mise à jour de la liste des images
	    if (b.getImagesBassin() != null && !b.getImagesBassin().isEmpty()) {
	        // Supprimer les images existantes uniquement si elles ne sont pas dans la nouvelle liste
	        existingBassin.getImagesBassin().removeIf(existingImage -> 
	            b.getImagesBassin().stream().noneMatch(newImage -> newImage.getIdImage().equals(existingImage.getIdImage()))
	        );

	        // Ajouter les nouvelles images
	        for (ImageBassin newImage : b.getImagesBassin()) {
	            if (newImage.getIdImage() == null) { // Nouvelle image
	                newImage.setBassin(existingBassin);
	                existingBassin.getImagesBassin().add(newImage);
	            }
	        }
	    }

	    // Sauvegarder le bassin mis à jour
	    return bassinRepository.save(existingBassin);
	}

	@Override
	public void deleteBassin(Bassin b) {
		bassinRepository.delete(b) ;	
	}
	 
	@Override
	public void deleteBassinById(Long id) {
	    Bassin b = getBassin(id);
	    if (b == null) {
	        throw new RuntimeException("Bassin not found with ID: " + id);
	    }

	    // Supprimer les fichiers images associés du dossier
	    if (b.getImagesBassin() != null && !b.getImagesBassin().isEmpty()) {
	        for (ImageBassin image : b.getImagesBassin()) {
	            String filePath = "C:/shared/images/" + image.getImagePath(); // Chemin complet du fichier
	            try {
	                Path path = Paths.get(filePath);
	                if (Files.exists(path)) {
	                    Files.delete(path); // Supprimer le fichier du dossier
	                    System.out.println("Fichier supprimé : " + filePath);
	                } else {
	                    System.out.println("Fichier introuvable : " + filePath);
	                }
	            } catch (IOException e) {
	                System.err.println("Erreur lors de la suppression du fichier : " + filePath);
	                e.printStackTrace();
	            }
	        }
	    }

	    // Supprimer les entrées des images dans la base de données
	    imageBassinRepository.deleteAll(b.getImagesBassin());

	    // Supprimer le bassin
	    bassinRepository.deleteById(id);
	}
	
	@Override
	public Bassin getBassin(Long id) {
		return bassinRepository.findById(id).get() ;
	}
	

	@Override
	public List<Bassin> getAllBassins() {
		return bassinRepository.findAll() ;
	}

	
	//new
	@Override
	public List<Bassin> findByNomBassin(String nom) {
		
		return bassinRepository.findByNomBassin(nom);
	}

	@Override
	public List<Bassin> findByNomBassinContains(String nom) {
		// TODO Auto-generated method stub
		return bassinRepository.findByNomBassinContains(nom);
	}

	@Override
	public List<Bassin> findByNomPrix(String nom, Double prix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Bassin> findByCategorie(Categorie c) {
		// TODO Auto-generated method stub
		return bassinRepository.findByCategorie(c);
	}

	@Override
	public List<Bassin> findByCategorieIdCategorie(Long id) {
		// TODO Auto-generated method stub
		return bassinRepository.findByCategorieIdCategorie(id);
	}

	@Override
	public List<Bassin> findByOrderByNomBassinAsc() {
		// TODO Auto-generated method stub
		return bassinRepository.findByOrderByNomBassinAsc();
	}

	@Override
	public List<Bassin> trierBassinsNomsPrix() {
		// TODO Auto-generated method stub
		return bassinRepository.trierBassinNomPrix();
	}

	
	public BassinDTO toBassinDTO(Bassin bassin) {
	    BassinDTO dto = new BassinDTO();
	    dto.setIdBassin(bassin.getIdBassin());
	    dto.setNomBassin(bassin.getNomBassin());
	    dto.setDescription(bassin.getDescription());
	    dto.setPrix(bassin.getPrix());
	    dto.setMateriau(bassin.getMateriau());
	    dto.setCouleur(bassin.getCouleur());
	    dto.setDimensions(bassin.getDimensions());
	    dto.setDisponible(bassin.isDisponible());
	    dto.setStock(bassin.getStock());

	   /* List<ImageDTO> imageDTOs = bassin.getImages().stream()
	        .map(image -> {
	            ImageDTO imageDTO = new ImageDTO();
	            imageDTO.setIdImage(image.getIdImage());
	            imageDTO.setName(image.getName());
	            imageDTO.setType(image.getType());
	            return imageDTO;
	        })
	        .collect(Collectors.toList());

	    dto.setImages(imageDTOs);*/
	    return dto;
	}
	
}
