import { Injectable } from '@angular/core';

import { Bassin } from '../models/bassin.models';
import { Categorie } from '../models/categorie.models';
import { ImageBassin } from '../models/image.models';

import { catchError, map, Observable } from 'rxjs';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../authentication/auth.service';
import { CategorieWrapper } from '../models/CategorieWrapper.models';
import { BassinPersonnalise } from '../models/bassinpersonnalise.models';
import { Accessoire } from '../models/accessoire.models';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class BassinService {

  /** Les variables **/
  bassins: Bassin[] = [];

  categories: Categorie[] = [];

  apiURL: string = 'http://localhost:8089/aquatresor/api';
  apiURLCategorie: string = 'http://localhost:8089/aquatresor/api/categories';

 
  constructor(private http: HttpClient, private authService: AuthService) { }

  
  //la liste des bassins
  listeBassin(): Observable<Bassin[]> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${this.authService.getToken()}`
    });
    return this.http.get<Bassin[]>(this.apiURL + "/all", { headers: headers });
  }

   // Charger les images pour un bassin spécifique
   chargerImagesPourBassin(bassin: Bassin): Bassin {
    if (bassin.imagesBassin && bassin.imagesBassin.length > 0) {
      bassin.imageStr = `${this.apiURL}/imagesBassin/getFS/${bassin.imagesBassin[0].imagePath}`;
    } else {
      bassin.imageStr = 'assets/default-image.webp';
    }
    return bassin;
  }

  // consulter un bassin
  consulterBassin(id: number): Observable<Bassin> {
    const url = `${this.apiURL}/getbyid/${id}`;
    let jwt = this.authService.getToken();
    jwt = "Bearer " + jwt;
    let httpHeaders = new HttpHeaders({ "Authorization": jwt });
    return this.http.get<Bassin>(url, { headers: httpHeaders });
  }

   // Ajouter un bassin
   ajouterBassin(bassin: Bassin): Observable<Bassin> {
    const headers = this.getHeaders();
    return this.http.post<Bassin>(`${this.apiURL}/addbassin`, bassin, { headers });
  }

  //
  // Ajouter un bassin
  ajouterBassinWithImg(bassinData: FormData): Observable<any> {
    return this.http.post(`${this.apiURL}/addBassinWithImages`, bassinData, {
      headers: new HttpHeaders({
        // Ne pas mettre 'Content-Type', FormData le gère automatiquement
        'Accept': 'application/json'
      })
    });
  }
  
  
  

  // supprimer bassin
  supprimerBassin(id: number): Observable<void> {
    const url = `${this.apiURL}/deletebassin/${id}`;
    let jwt = this.authService.getToken();
    jwt = "Bearer " + jwt;
    let httpHeaders = new HttpHeaders({ "Authorization": jwt });
    return this.http.delete<void>(url, { headers: httpHeaders });
  }

  // update un bassin
  updateBassin(b: Bassin): Observable<Bassin> {
    let jwt = this.authService.getToken();
    jwt = "Bearer " + jwt;
    let httpHeaders = new HttpHeaders({ "Authorization": jwt });
    const url = `${this.apiURL}/updatebassin/${b.idBassin}`; // Include ID if needed
    return this.http.put<Bassin>(url, b, { headers: httpHeaders });
  }

  updateBassinWithImg(bassin: Bassin, files: File[]): Observable<Bassin> {
    const formData = new FormData();
    formData.append('bassin', JSON.stringify(bassin));
  
    files.forEach((file, index) => {
      if (file) {
        formData.append(`files`, file);
      }
    });
  
    return this.http.post<Bassin>(`${this.apiURL}/updateBassinWithImg`, formData);
  }
  
  
  
  
  
  

  /** Image **/
  uploadImage(file: File, filename: string): Observable<ImageBassin> {
    const imageFormData = new FormData();
    imageFormData.append('image', file, filename);
    const url = `${this.apiURL + '/image/upload'}`;
    return this.http.post<ImageBassin>(url, imageFormData);
  }

  loadImage(id: number): Observable<ImageBassin> {
    const url = `${this.apiURL + '/image/get/info'}/${id}`;
    return this.http.get<ImageBassin>(url);
  }

  uploadImageBassin(file: File, filename: string, idBassin: number): Observable<any> {
    const imageFormData = new FormData();
    imageFormData.append('image', file, filename);
    const url = `${this.apiURL + '/image/uploadImageB'}/${idBassin}`;

    return this.http.post(url, imageFormData);
  }

  /******Les lignes de codes pour stocker une images dans le file *******/
  uploadImageFS(file: File, idBassin: number): Observable<any> {
    console.log(`📤 Upload de l'image:`, file.name, ` pour bassin ID: ${idBassin}`);
    const imageFormData = new FormData();

  // Générer le nom de fichier selon la logique idBassin_idImage.extension
  //const originalFileName = file.name;
  //const extension = originalFileName.split('.').pop(); // Récupérer l'extension du fichier
  //const fileName = `${idBassin}_${imageNumber}.${extension}`;

  // Ajouter le fichier avec le nom correct
  //imageFormData.append('image', file, fileName);
  imageFormData.append('image', file);

  //console.log(`Uploading file: ${fileName} for bassin ID: ${idBassin}`); // Log pour vérifier le nom du fichier

  const url = `${this.apiURL}/imagesBassin/uploadFS/${idBassin}`;
  console.log(`URL:`, url);
  return this.http.post(url, imageFormData);
  }

  supprimerImage(id: number): Observable<void> {
    const url = `${this.apiURL}/imagesBassin/delete/${id}`;
    let jwt = this.authService.getToken();
    jwt = "Bearer " + jwt;
    let httpHeaders = new HttpHeaders({ "Authorization": jwt });
    return this.http.delete<void>(url, { headers: httpHeaders });
  }

   // Obtenir la liste des catégories
  listeCategories(): Observable<Categorie[]> {
    const headers = this.getHeaders();
    return this.http.get<Categorie[]>(`${this.apiURL}/categories`, { headers });
  }

  // Méthode pour générer les en-têtes avec le token JWT
  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  //Bassin Personnalisé
  // Ajoutez cette méthode pour ajouter un bassin personnalisé
  ajouterBassinPersonnalise(formData: FormData, idBassin: number): Observable<BassinPersonnalise> {
    const headers = this.getHeaders();
    return this.http.post<BassinPersonnalise>(`${this.apiURL}/bassinpersonnalise/ajouterBassinPersonnalise/${idBassin}`,
      formData
    );
  }  

  // Fonction pour extraire le nom du fichier à partir du chemin complet
private getFileNameFromPath(filePath: string): string {
  // Utiliser une expression régulière pour extraire le nom du fichier
  const fileName = filePath.split('\\').pop() || filePath.split('/').pop() || filePath;
  console.log("nom de file est: ",fileName)
  return fileName;
}

  // consulter un bassin Personnalise
  consulterBassinPersonnalise(id: number): Observable<BassinPersonnalise> {
    return this.http.get<BassinPersonnalise>(`${this.apiURL}/bassinpersonnalise/detailBassinPersonnalise/${id}`)
      .pipe(
        map(bassinPersonnalise => {
          // Construire l'URL complète pour chaque accessoire
          bassinPersonnalise.accessoires = bassinPersonnalise.accessoires.map(accessoire => {
            const fileName = this.getFileNameFromPath(accessoire.imagePath); // Extraire le nom du fichier
            accessoire.imageUrl = `${this.apiURL}/imagespersonnalise/${fileName}`; // Construire l'URL complète
            return accessoire;
          });
          return bassinPersonnalise;
        })
      );
  }

  supprimerBassinPersonnalise(idBassinPersonnalise: number): Observable<BassinPersonnalise> {
    const headers = this.getHeaders();
    return this.http.delete<BassinPersonnalise>(`${this.apiURL}/bassinpersonnalise/supprimerBassinPersonnalise/${idBassinPersonnalise}`);
  }

  //Récupère l'id de bassin personnalise en entrant dans l'url l'id de bassin
  getBassinPersonnaliseByBassinId(idBassin: number): Observable<any> {
    return this.http.get<any>(`${this.apiURL}/bassinpersonnalise/getBassinPersonnaliseByBassin/${idBassin}`);
  }

  // Mettre à jour un bassin personnalisé
mettreAJourBassinPersonnalise(idBassinPersonnalise: number, formData: FormData): Observable<BassinPersonnalise> {
  console.log("Après mise à jour de l'ID :", idBassinPersonnalise); // Correct placement
  
  return this.http.put<BassinPersonnalise>(
    `${this.apiURL}/bassinpersonnalise/mettreAJourBassinPersonnalise/${idBassinPersonnalise}`,
    formData
  );
}

getAccessoireImages(idBassinPersonnalise: number): Observable<string[]> {
  return this.http.get<string[]>(`${this.apiURL}/bassinpersonnalise/${idBassinPersonnalise}/accessoires/images`);
}


// Dans le fichier bassin.service.ts

getBassinPersonnaliseOptions(idBassin: number): Observable<{
  materiaux: string[],
  dimensions: string[],
  accessoires: Accessoire[]
}> {
  const url = `${this.apiURL}/bassinpersonnalise/options/${idBassin}`;
  const headers = this.getHeaders(); // Utilisez les en-têtes avec le token JWT

  return this.http.get<{
    materiaux: string[],
    dimensions: string[],
    accessoires: any[]
  }>(url, { headers }).pipe(
    map(options => {
      // Générer l'URL complète pour chaque accessoire
      options.accessoires = options.accessoires.map(accessoire => {
        const fileName = this.getFileNameFromPath(accessoire.imagePath); // Extraire le nom du fichier
        accessoire.imageUrl = `${this.apiURL}/imagespersonnalise/${fileName}`; // Construire l'URL complète
        return accessoire;
      });
      return options;
    }),
    catchError((error: HttpErrorResponse) => {
      console.error('Erreur lors de la récupération des options de personnalisation', error);
      throw error; // Propager l'erreur pour la gestion dans le composant
    })
  );
}

}
