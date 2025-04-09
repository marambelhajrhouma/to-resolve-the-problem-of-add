package com.example.gestionbassins.repos;

import com.example.gestionbassins.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.gestionbassins.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

@Repository 
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    // Trouver les promotions qui se chevauchent pour un bassin spécifique
    @Query("SELECT p FROM Promotion p JOIN p.bassins b WHERE b.idBassin = :bassinId " +
           "AND ((p.dateDebut <= :dateFin AND p.dateFin >= :dateDebut) " +
           "OR (p.dateDebut >= :dateDebut AND p.dateDebut <= :dateFin)) " +
           "AND (:promotionId IS NULL OR p.idPromotion != :promotionId)")
    List<Promotion> findOverlappingPromotionsForBassin(
        @Param("bassinId") Long bassinId, 
        @Param("dateDebut") Date dateDebut, 
        @Param("dateFin") Date dateFin,
        @Param("promotionId") Long promotionId);
        
    // Trouver les promotions qui se chevauchent pour une catégorie spécifique
    @Query("SELECT p FROM Promotion p JOIN p.categories c WHERE c.idCategorie = :categorieId " +
           "AND ((p.dateDebut <= :dateFin AND p.dateFin >= :dateDebut) " +
           "OR (p.dateDebut >= :dateDebut AND p.dateDebut <= :dateFin)) " +
           "AND (:promotionId IS NULL OR p.idPromotion != :promotionId)")
    List<Promotion> findOverlappingPromotionsForCategorie(
        @Param("categorieId") Long categorieId, 
        @Param("dateDebut") Date dateDebut, 
        @Param("dateFin") Date dateFin,
        @Param("promotionId") Long promotionId);
}