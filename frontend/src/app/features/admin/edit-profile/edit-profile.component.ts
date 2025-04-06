import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/authentication/auth.service';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css'],
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-10px)' }),
        animate('0.5s ease-in-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('0.5s ease-in-out', style({ opacity: 0, transform: 'translateY(-10px)' }))
      ])
    ])
  ]
})
export class EditProfileComponent implements OnInit {
  emailForm: FormGroup;
  passwordForm: FormGroup;
  message: string = '';
  isSuccess: boolean = false;
  showCurrentPassword: boolean = false;
  showNewPassword: boolean = false;
  showConfirmPassword: boolean = false;
  currentEmail: string = '';
  activeTab: string = 'email';

  constructor(
    private fb: FormBuilder,
    public authService: AuthService,
    private router: Router
  ) {
    // Initialisation des formulaires
    this.emailForm = this.fb.group({
      newEmail: ['', [Validators.email, Validators.required]],
      currentPassword: ['', [Validators.required]],
    });

    this.passwordForm = this.fb.group(
      {
        currentPassword: ['', [Validators.required]],
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validator: this.passwordMatchValidator }
    );
  }

  ngOnInit(): void {
    this.loadCurrentEmail();
  }

  private loadCurrentEmail(): void {
    this.authService.getUserProfile().subscribe({
      next: (user: any) => {
        this.currentEmail = user.email;
      },
      error: (error: any) => {
        this.showMessage('Erreur lors de la récupération des informations', false);
      },
    });
  }

  switchTab(tab: string): void {
    this.activeTab = tab;
  }

  passwordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { mismatch: true };
  }

  toggleShowCurrentPassword(): void {
    this.showCurrentPassword = !this.showCurrentPassword;
  }

  toggleShowNewPassword(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onUpdateEmail(): void {
    if (this.emailForm.invalid) {
      this.showMessage('Veuillez remplir tous les champs obligatoires.', false);
      return;
    }

    const { newEmail, currentPassword } = this.emailForm.value;
    const username = this.authService.loggedUser;

    this.authService.updateProfile(username, newEmail, undefined, currentPassword).subscribe({
      next: () => {
        this.showMessage('Email mis à jour avec succès.', true);
        this.emailForm.reset();
        this.loadCurrentEmail();
      },
      error: (error: any) => {
        this.showMessage(error.error.message || 'Une erreur est survenue.', false);
      },
    });
  }

  onUpdatePassword(): void {
    if (this.passwordForm.invalid) {
      this.showMessage('Veuillez remplir tous les champs obligatoires.', false);
      return;
    }

    const { newPassword, currentPassword } = this.passwordForm.value;
    const username = this.authService.loggedUser;

    this.authService.updateProfile(username, undefined, newPassword, currentPassword).subscribe({
      next: () => {
        this.showMessage('Mot de passe mis à jour avec succès.', true);
        this.passwordForm.reset();
      },
      error: (error: any) => {
        this.showMessage(error.error.message || 'Une erreur est survenue.', false);
      },
    });
  }

  private showMessage(message: string, isSuccess: boolean): void {
    this.message = message;
    this.isSuccess = isSuccess;
    setTimeout(() => (this.message = ''), 2000); // Disparaît après 2 secondes
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/admin/signin']);
  }
}