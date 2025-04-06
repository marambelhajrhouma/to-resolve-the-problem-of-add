package com.example.gestionbassins.entities;

import org.springframework.data.rest.core.config.Projection;

@Projection(name="nomEvenement", types= {Bassin.class})
public interface BassinProjection {
	public String getNomBassin();
}
