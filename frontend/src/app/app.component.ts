import { Component } from '@angular/core';
import { AuthService } from './core/authentication/auth.service';
import { Router } from '@angular/router';
import { LoadingService } from './core/services/loading.service';
import { Observable } from 'rxjs'; // Importez Observable

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'] // Utilisez styleUrls au lieu de styleUrl
})
export class AppComponent {
  title = 'frontend';
  isLoading$: Observable<boolean>; // Déclarez isLoading$ comme un Observable

  constructor(
    private authService: AuthService,
    private router: Router,
    private loadingService: LoadingService
  ) {
    this.isLoading$ = this.loadingService.isLoading$; // Initialisez isLoading$
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/admin/signin']);
  }
}