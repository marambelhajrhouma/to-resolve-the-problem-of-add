import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Bassin } from '../models/bassin.models';
import { BassinService } from './bassin.service';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private cartItemsSubject = new BehaviorSubject<{ bassin: Bassin; quantity: number }[]>([]);
  cartItems$: Observable<{ bassin: Bassin; quantity: number }[]> = this.cartItemsSubject.asObservable();

  constructor(private bassinService: BassinService) {
    // Récupération du panier depuis le localStorage au démarrage
    this.loadCartFromLocalStorage();
  }

  // Méthode privée pour charger les données du panier depuis localStorage
  private loadCartFromLocalStorage(): void {
    if (typeof window !== 'undefined') {
      const savedCart = localStorage.getItem('cart');
      if (savedCart) {
        try {
          const cartItems = JSON.parse(savedCart);
          this.cartItemsSubject.next(cartItems);
        } catch (e) {
          console.error('Erreur lors du chargement du panier:', e);
        }
      }
    }
  }

  // Méthode privée pour sauvegarder le panier dans localStorage
  private saveCartToLocalStorage(): void {
    if (typeof window !== 'undefined') {
      localStorage.setItem('cart', JSON.stringify(this.cartItemsSubject.value));
    }
  }

  // Récupérer les éléments du panier
  getCartItems(): { bassin: Bassin; quantity: number }[] {
    return this.cartItemsSubject.value;
  }

  // Ajouter un élément au panier
  addToCart(bassin: Bassin): void {
    console.log('Bassin avant chargement de l\'image:', bassin);
    bassin = this.bassinService.chargerImagesPourBassin(bassin);
    console.log('Bassin après chargement de l\'image:', bassin);
  
    const currentItems = this.cartItemsSubject.value;
    const existingItem = currentItems.find((item) => item.bassin.idBassin === bassin.idBassin);
  
    if (existingItem) {
      existingItem.quantity += 1;
    } else {
      currentItems.push({ bassin, quantity: 1 });
    }
  
    this.cartItemsSubject.next([...currentItems]);
    this.saveCartToLocalStorage();
  }

  // Mettre à jour la quantité d'un élément
  updateQuantity(bassin: Bassin, quantity: number): void {
    const currentItems = this.cartItemsSubject.value;
    const itemToUpdate = currentItems.find((item) => item.bassin.idBassin === bassin.idBassin);

    if (itemToUpdate) {
      itemToUpdate.quantity = quantity;
      this.cartItemsSubject.next([...currentItems]); // Mettre à jour le BehaviorSubject
      this.saveCartToLocalStorage(); // Sauvegarder dans localStorage
    }
  }

  // Supprimer un élément du panier
  removeFromCart(bassin: Bassin): void {
    const currentItems = this.cartItemsSubject.value.filter((item) => item.bassin.idBassin !== bassin.idBassin);
    this.cartItemsSubject.next([...currentItems]); // Mettre à jour le BehaviorSubject
    this.saveCartToLocalStorage(); // Sauvegarder dans localStorage
  }

  // Vider complètement le panier
  clearCart(): void {
    this.cartItemsSubject.next([]);
    this.saveCartToLocalStorage(); // Sauvegarder dans localStorage
  }

  // Obtenir le nombre total d'articles dans le panier (quantités incluses)
  getTotalQuantity(): number {
    return this.cartItemsSubject.value.reduce((total, item) => total + item.quantity, 0);
  }

  // Obtenir le total du panier en valeur
  getTotalPrice(): number {
    return this.cartItemsSubject.value.reduce(
      (total, item) => total + item.bassin.prix * item.quantity,
      0
    );
  }
}