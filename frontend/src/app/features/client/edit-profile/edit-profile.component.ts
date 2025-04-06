import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Swal from 'sweetalert2';
import { AuthService } from '../../../core/authentication/auth.service';

@Component({
  selector: 'app-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css']
})
export class EditProfileComponent implements OnInit {
  editProfileForm: FormGroup;
  profileImage: string | ArrayBuffer | null = null;
  selectedFile: File | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
   private http: HttpClient
  ) {
    this.editProfileForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      currentPassword: ['', Validators.required],
      newPassword: [''],
      confirmPassword: [''],
      profileImage: ['']
    });
  }

  ngOnInit(): void {
    this.authService.getUserProfile().subscribe((user: any) => {
      this.editProfileForm.patchValue({
        username: user.username,
        email: user.email,
        profileImage: user.profileImage
      });
      this.profileImage = user.profileImage;
    });
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.profileImage = reader.result;
      };
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    if (this.editProfileForm.valid) {
      const { username, email, currentPassword, newPassword, confirmPassword } = this.editProfileForm.value;
      if (newPassword !== confirmPassword) {
        Swal.fire('Erreur', 'Les mots de passe ne correspondent pas', 'error');
        return;
      }

      const formData = new FormData();
      formData.append('username', username);
      formData.append('file', this.selectedFile as Blob);

      this.http.post('/users/uploadProfileImage', formData).subscribe(
        (response: any) => {
          this.authService.updateProfile(username, email, newPassword, currentPassword, response.imagePath).subscribe(
            (response) => {
              Swal.fire('Succès', 'Profil mis à jour avec succès', 'success');
            },
            (error) => {
              Swal.fire('Erreur', error.error.message || 'Une erreur est survenue', 'error');
            }
          );
        },
        (error) => {
          Swal.fire('Erreur', 'Erreur lors de l\'upload de l\'image', 'error');
        }
      );
    }
  }
}