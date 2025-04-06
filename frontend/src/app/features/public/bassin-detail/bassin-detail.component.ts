import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Bassin } from '../../../core/models/bassin.models';
import { ActivatedRoute, Router } from '@angular/router';
import { BassinService } from '../../../core/services/bassin.service';
import { CartService } from '../../../core/services/cart.service';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-bassin-detail',
  templateUrl: './bassin-detail.component.html',
  styleUrls: ['./bassin-detail.component.css'],
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(20px)' }),
        animate('0.4s ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('0.3s ease-in', style({ opacity: 0, transform: 'translateY(20px)' }))
      ])
    ]),
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(-100%)' }),
        animate('0.5s ease-out', style({ transform: 'translateX(0)' }))
      ]),
      transition(':leave', [
        animate('0.4s ease-in', style({ transform: 'translateX(-100%)' }))
      ])
    ])
  ]
})
export class BassinDetailComponent implements OnInit {
  bassin: Bassin | undefined;
  selectedImage: string | undefined;
  isZoomed: boolean = false;
  imagePreviews: string[] = [];
  customizationForm: FormGroup;
  isCustomizing: boolean = false;
  customizationStep: number = 1;
  totalSteps: number = 3;
  isCustomizationComplete: boolean = false;
  customizationSummary: any = {};
  
  constructor(
    private route: ActivatedRoute,
    private bassinService: BassinService,
    private cartService: CartService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.customizationForm = this.fb.group({
      length: ['', [Validators.required, Validators.min(1)]],
      width: ['', [Validators.required, Validators.min(1)]],
      depth: ['', [Validators.required, Validators.min(1)]],
      couleur: ['bleu', Validators.required],
      materiau: ['beton', Validators.required],
      options: [''],
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.bassinService.consulterBassin(+id).subscribe({
        next: (bassin) => {
          this.bassin = bassin;
          if (bassin.imagesBassin && bassin.imagesBassin.length > 0) {
            this.imagePreviews = bassin.imagesBassin.map(
              (img) => `http://localhost:8089/aquatresor/api/imagesBassin/getFS/${img.imagePath}`
            );
            this.selectedImage = this.imagePreviews[0];
          } else {
            this.selectedImage = 'assets/default-image.webp';
          }
        },
        error: (err) => {
          console.error('Erreur lors du chargement du bassin', err);
        },
      });
    }
  }

  startCustomization(): void {
    this.isCustomizing = true;
    this.customizationStep = 1;
    // Initialiser les valeurs par défaut
    this.customizationForm.patchValue({
      length: 2,
      width: 2,
      depth: 1,
      couleur: 'bleu',
      materiau: 'beton',
      options: ''
    });
  }

  cancelCustomization(): void {
    this.isCustomizing = false;
    this.customizationStep = 1;
    this.isCustomizationComplete = false;
  }

  nextStep(): void {
    if (this.isStepValid()) {
      if (this.customizationStep < this.totalSteps) {
        this.customizationStep++;
      } else {
        this.completeCustomization();
      }
    }
  }

  previousStep(): void {
    if (this.customizationStep > 1) {
      this.customizationStep--;
    } else {
      this.cancelCustomization();
    }
  }

  isStepValid(): boolean {
    if (this.customizationStep === 1) {
      return !!(
        this.customizationForm.get('length')?.valid &&
        this.customizationForm.get('width')?.valid &&
        this.customizationForm.get('depth')?.valid
      );
    } else if (this.customizationStep === 2) {
      return !!(
        this.customizationForm.get('couleur')?.valid &&
        this.customizationForm.get('materiau')?.valid
      );
    }
    return true;
  }

  completeCustomization(): void {
    this.isCustomizationComplete = true;
    this.customizationSummary = {
      dimensions: `${this.customizationForm.value.length}m x ${this.customizationForm.value.width}m x ${this.customizationForm.value.depth}m`,
      couleur: this.customizationForm.value.couleur,
      materiau: this.customizationForm.value.materiau,
      options: this.customizationForm.value.options || 'Aucune',
      prixEstime: this.calculateCustomPrice()
    };
  }

  calculateCustomPrice(): number {
    if (!this.bassin) return 0;
    
    let basePrice = this.bassin.prix;
    const volume = this.customizationForm.value.length * 
                   this.customizationForm.value.width * 
                   this.customizationForm.value.depth;
    
    // Ajustement basé sur le volume
    const volumeMultiplier = volume / 4; // Supposons 4m³ comme volume standard
    
    // Ajustements basés sur le matériau
    const materiauMultipliers: { [key: string]: number } = {
      'beton': 1.0,
      'fibre': 1.2,
      'acier': 1.5,
      'bois': 1.3
    };;
    
    // Calcul du prix final
    const materiau = this.customizationForm.value.materiau;
    const materiauMultiplier = materiauMultipliers[materiau as keyof typeof materiauMultipliers] || 1.0;
    
    return Math.round(basePrice * volumeMultiplier * materiauMultiplier);
  }

  onSubmit(): void {
    if (this.customizationForm.valid && this.bassin) {
      // Création d'un objet avec les propriétés personnalisées
      const customProps = {
        dimensions: `${this.customizationForm.value.length}m x ${this.customizationForm.value.width}m x ${this.customizationForm.value.depth}m`,
        couleur: this.customizationForm.value.couleur,
        materiau: this.customizationForm.value.materiau,
        options: this.customizationForm.value.options,
        prix: this.calculateCustomPrice() // Prix personnalisé
      };
      
      // Création d'un nouvel objet Bassin avec le même prototype
      const customizedBassin = Object.assign(
        Object.create(Object.getPrototypeOf(this.bassin)),
        this.bassin,
        customProps as Partial<Bassin>
      );
      
      this.cartService.addToCart(customizedBassin);
      this.router.navigate(['/cart']);
    }
  }

  addToCart(): void {
    if (this.bassin) {
      this.cartService.addToCart(this.bassin);
      // Afficher un message de confirmation
      this.showToast('Produit ajouté au panier avec succès!');
    }
  }
  
  showToast(message: string): void {
    // Cette méthode pourrait être implémentée avec un service Toast
    console.log('Toast message:', message);
    // Exemple: this.toastService.show(message, 'success');
  }

  addToFavorites(): void {
    if (this.bassin) {
      console.log('Ajout aux favoris :', this.bassin);
      this.showToast('Produit ajouté aux favoris!');
    }
  }

  changeImage(imageUrl: string): void {
    this.selectedImage = imageUrl;
    this.isZoomed = false;
  }

  toggleZoom(): void {
    this.isZoomed = !this.isZoomed;
  }

  viewIn3D(): void {
    console.log('Voir en 3D');
    // Implémenter la logique pour afficher en 3D
  }

  viewInAR(): void {
    console.log('Voir en AR');
    // Implémenter la logique pour afficher en AR
  }
  
  getStepLabel(step: number): string {
    switch(step) {
      case 1: return 'Dimensions';
      case 2: return 'Matériaux';
      case 3: return 'Options';
      default: return '';
    }
  }
  
  getColorDisplay(color: string): any {
    const colorMap: {[key: string]: string} = {
      'bleu': '#3498db',
      'blanc': '#f8f9fa',
      'noir': '#2c3e50',
      'vert': '#2ecc71'
    };
    return { backgroundColor: colorMap[color] || color };
  }
}