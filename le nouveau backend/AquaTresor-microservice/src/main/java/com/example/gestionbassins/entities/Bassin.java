package com.example.gestionbassins.entities;


import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Bassin {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idBassin;

    private String nomBassin;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    
    @Column(name = "prix", nullable = false)
    private Double prix;
    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean archive; 
    
    private String materiau; 
    private String couleur;
    private String dimensions; 
    
    private boolean disponible; // Indique si le bassin est en stock

    private int stock;
    
    @ManyToOne 
	private Categorie categorie;    
	
	//ImageBassinCode
 //   @OneToMany(mappedBy = "bassin", fetch = FetchType.EAGER)
    //@JsonManagedReference
    @OneToMany(mappedBy = "bassin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageBassin> imagesBassin;
    
    @Column(name = "image_path")
    private String imagePath;
    
    @Column(name = "image_3d_path")
    private String image3DPath;   
    
    public Bassin(String nomBassin, String description, double prix, String materiau, String couleur,
			String dimensions, boolean disponible, int stock, Categorie categorie) {
		super();
		this.nomBassin = nomBassin;
		this.description = description;
		this.prix = prix;
		this.materiau = materiau;
		this.couleur = couleur;
		this.dimensions = dimensions;
		this.disponible = disponible;
		this.stock = stock;
		this.categorie = categorie;
	}

    @Override
    public String toString() {
        return "Bassin{" +
                "idBassin=" + idBassin +
                ", nomBassin='" + nomBassin + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", materiau='" + materiau + '\'' +
                ", couleur='" + couleur + '\'' +
                ", dimensions='" + dimensions + '\'' +
                ", disponible=" + disponible +
                ", stock=" + stock +
                ", categorie=" + (categorie != null ? categorie.getIdCategorie() : null) +
                ", imagePath='" + imagePath + '\'' +
                ", image3DPath='" + image3DPath + '\'' +
                '}';
    }


}

