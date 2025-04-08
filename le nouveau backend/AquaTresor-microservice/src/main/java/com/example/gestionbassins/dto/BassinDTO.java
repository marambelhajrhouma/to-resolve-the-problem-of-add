package com.example.gestionbassins.dto;

import java.util.List;

public class BassinDTO {
	private Long idBassin;
    private String nomBassin;
    private String description;
    private Double prix;
    private String materiau;
    private String couleur;
    private String dimensions;
    private boolean disponible;
    private int stock;
    //private List<ImageDTO> images;
    
    
	public Long getIdBassin() {
		return idBassin;
	}
	public void setIdBassin(Long idBassin) {
		this.idBassin = idBassin;
	}
	public String getNomBassin() {
		return nomBassin;
	}
	public void setNomBassin(String nomBassin) {
		this.nomBassin = nomBassin;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Double getPrix() {
		return prix;
	}
	public void setPrix(Double prix) {
		this.prix = prix;
	}
	public String getMateriau() {
		return materiau;
	}
	public void setMateriau(String materiau) {
		this.materiau = materiau;
	}
	public String getCouleur() {
		return couleur;
	}
	public void setCouleur(String couleur) {
		this.couleur = couleur;
	}
	public String getDimensions() {
		return dimensions;
	}
	public void setDimensions(String dimensions) {
		this.dimensions = dimensions;
	}
	public boolean isDisponible() {
		return disponible;
	}
	public void setDisponible(boolean disponible) {
		this.disponible = disponible;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	/*public List<ImageDTO> getImages() {
		return images;
	}
	public void setImages(List<ImageDTO> images) {
		this.images = images;
	}*/

    // Getters and Setters
    
    
}