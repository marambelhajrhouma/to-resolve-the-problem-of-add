package com.example.gestionbassins.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPromotion;

    @NotNull(message = "Le nom de la promotion est obligatoire")
    private String nomPromotion;

    @NotNull(message = "Le taux de réduction est obligatoire")
    private Double tauxReduction;

    @NotNull(message = "La date de début est obligatoire")
    private Date dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private Date dateFin;

    @ManyToMany
    @JoinTable(
        name = "promotion_bassin",
        joinColumns = @JoinColumn(name = "idPromotion"),
        inverseJoinColumns = @JoinColumn(name = "idBassin")
    )
    private List<Bassin> bassins;

    @ManyToMany
    @JoinTable(
        name = "promotion_categorie",
        joinColumns = @JoinColumn(name = "idPromotion"),
        inverseJoinColumns = @JoinColumn(name = "idCategorie")
    )
    private List<Categorie> categories;

    // Validation personnalisée pour s'assurer qu'au moins un bassin ou une catégorie est sélectionné
    @AssertTrue(message = "Au moins un bassin ou une catégorie doit être sélectionné")
    public boolean isBassinOrCategorieSelected() {
        return (bassins != null && !bassins.isEmpty()) || (categories != null && !categories.isEmpty());
    }
}