import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Categorie } from '../models/categorie.models';
import { AuthService } from '../authentication/auth.service';

@Injectable({
  providedIn: 'root',
})
export class CategorieService {
  private apiUrl = 'http://localhost:8089/aquatresor/api/categories';

  constructor(private http: HttpClient, private authService: AuthService) {}

  // Récupérer toutes les catégories
  getAllCategories(): Observable<Categorie[]> {
    const headers = this.getHeaders();
    return this.http.get<Categorie[]>(this.apiUrl, { headers });
  }

  // Récupérer une catégorie par son ID
  getCategorieById(id: number): Observable<Categorie> {
    const headers = this.getHeaders();
    return this.http.get<Categorie>(`${this.apiUrl}/${id}`, { headers });
  }

  // Ajouter une nouvelle catégorie
  addCategorie(categorie: Categorie): Observable<Categorie> {
    const headers = this.getHeaders();
    return this.http.post<Categorie>(`${this.apiUrl}/addCategorie`, categorie, {
      headers,
    });
  }

  // Mettre à jour une catégorie existante
  updateCategorie(id: number, categorie: Categorie): Observable<Categorie> {
    const headers = this.getHeaders();
    return this.http.put<Categorie>(
      `${this.apiUrl}/updateCategorie/${id}`,
      categorie,
      { headers }
    );
  }

  // Supprimer une catégorie
  deleteCategorie(id: number): Observable<void> {
    const headers = this.getHeaders();
    return this.http.delete<void>(`${this.apiUrl}/deleteCategorie/${id}`, {
      headers,
    });
  }

  // Méthode pour générer les en-têtes avec le token JWT
  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }
}
