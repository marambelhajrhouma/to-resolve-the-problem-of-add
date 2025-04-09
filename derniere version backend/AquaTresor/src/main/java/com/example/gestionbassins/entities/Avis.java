package com.example.gestionbassins.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Avis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAvis;

    private String nom; // Nom de l'utilisateur (connecté ou anonyme)
    private String message; // Message de l'avis
    private int note; // Note de l'avis (entre 1 et 5)

    @ManyToOne
    @JoinColumn(name = "idBassin")
    private Bassin bassin; // Référence au bassin concerné

    @Column(name = "user_id")
    private Long userId; // Référence à l'utilisateur connecté (null si anonyme)

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSoumission; // Date de soumission de l'avis

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModification; // Date de la dernière modification

    @ElementCollection
    private List<String> historiqueMessages; // Historique des messages modifiés

    public Avis(String nom, String message, int note, Bassin bassin, Long userId) {
        this.nom = nom;
        this.message = message;
        this.note = note;
        this.bassin = bassin;
        this.userId = userId;
        this.dateSoumission = new Date(); // Date de soumission par défaut
        this.historiqueMessages = new ArrayList<>();
    }

    public void addHistoriqueMessage(String message) {
        this.historiqueMessages.add(message);
    }
}