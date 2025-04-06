import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { JwtHelperService } from '@auth0/angular-jwt';
import Swal from 'sweetalert2';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { User } from '../models/user.model';
import { Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { SocialUser } from '@abacritt/angularx-social-login';
import { BehaviorSubject, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  apiURL: string = 'http://localhost:8002/users';
  oauthURL = 'http://localhost:8080/oauth2';

  token!: string;

  public loggedUser!: string;
  public isloggedIn: Boolean = false;
  public roles!: string[];
  public regitredUser: User = new User();
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.isLoggedIn);
  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  setLoggedInStatus(isLoggedIn: boolean) {
    this.isLoggedInSubject.next(isLoggedIn);
  }

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router,
    private http: HttpClient,
    private jwtHelper: JwtHelperService
  ) {
    this.loadToken();
  }

  public get isLoggedIn(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('jwt');
      return token ? !this.jwtHelper.isTokenExpired(token) : false;
    }
    return false;
  }

  loadToken() {
    if (isPlatformBrowser(this.platformId)) {
      this.token = localStorage.getItem('jwt')!;
      if (this.token) {
        this.decodeJWT();
        this.isloggedIn = true;
        this.setLoggedInStatus(true);
      } else {
        this.isloggedIn = false;
        this.setLoggedInStatus(false);
      }
    }
  }

  getToken(): string {
    return this.token;
  }

  saveToken(jwt: string) {
    if (isPlatformBrowser(this.platformId)) {
      if (jwt?.startsWith('Bearer ')) {
        jwt = jwt.substring(7);
      }
      localStorage.setItem('jwt', jwt);
      this.token = jwt;
      this.decodeJWT();
    }
  }

  private decodeJWT() {
    if (this.token) {
      try {
        const decodedToken = this.jwtHelper.decodeToken(this.token);
        console.log('Decoded Token:', decodedToken);
        this.roles = decodedToken.roles || [];
        this.loggedUser = decodedToken.sub;
        this.isloggedIn = true;
      } catch (error) {
        console.error('Error decoding JWT:', error);
        this.logout();
      }
    }
  }

  deJWT(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const decodedToken = JSON.parse(atob(base64));
      return decodedToken;
    } catch (error) {
      console.error('Error manually decoding JWT:', error);
      return {};
    }
  }

  getUserRoles(): string[] {
    const token = localStorage.getItem('jwt');
    if (token) {
      const decodedToken = this.deJWT(token);
      return decodedToken.roles || [];
    }
    return [];
  }

  isTokenExpired(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      const token = localStorage.getItem('jwt');
      if (!token) return true;
      return this.jwtHelper.isTokenExpired(token);
    }
    return true;
  }

  getUserRole(): string | undefined {
    return this.roles?.[0];
  }

  socialLogin(socialUser: SocialUser): Observable<any> {
    const payload = {
      email: socialUser.email,
      name: socialUser.name,
      idToken: socialUser.idToken,
      provider: socialUser.provider,
    };

    return this.http
      .post<any>(`${this.oauthURL}/social-login`, payload, {
        observe: 'response',
      })
      .pipe(
        tap((response) => {
          const jwt = response.headers.get('Authorization');
          if (jwt) {
            this.saveToken(jwt);
            this.setLoggedInStatus(true);

            if (socialUser.email) {
              // Vérifier si l'utilisateur est déjà vérifié
              this.getUserProfile().subscribe((user: User) => {
                if (user.enabled) {
                  this.redirectBasedOnRole();
                } else {
                  localStorage.setItem(
                    'pendingVerificationEmail',
                    socialUser.email
                  );
                  this.router.navigate(['/verifEmail']);
                }
              });
            } else {
              console.warn('No email available from social login');
              this.redirectBasedOnRole();
            }
          } else {
            console.warn('No JWT token received from social login');
          }
        }),
        catchError((error) => {
          console.error('Social login error:', error);
          Swal.fire('Error', 'Social login failed', 'error');
          return of(null);
        })
      );
  }

  verifyEmail(email: string, code: string): Observable<any> {
    return this.http
      .post(
        `${this.apiURL}/verify-email`,
        { email, code },
        { observe: 'response' }
      )
      .pipe(
        tap((response) => {
          const jwt = response.headers.get('Authorization');
          if (jwt) {
            this.saveToken(jwt);
            this.setLoggedInStatus(true);
            this.router.navigate(['/homepage']);
          }
        }),
        catchError((error) => {
          console.error('Email verification error:', error);
          Swal.fire(
            'Error',
            error.error?.message || 'Email verification failed',
            'error'
          );
          return of(null);
        })
      );
  }

  // Ajouter cette méthode pour la redirection basée sur le rôle
  private redirectBasedOnRole() {
    if (this.isAdmin()) {
      this.router.navigate(['/admin/dashboard']);
    } else if (this.isInstaller()) {
      this.router.navigate(['/installer-home']);
    } else if (this.isClient()) {
      this.router.navigate(['/homepage']);
    } else {
      Swal.fire('Error', 'No role assigned', 'error');
      this.logout();
    }
  }

  login(credentials: {
    username: string;
    password: string;
  }): Observable<HttpResponse<any>> {
    return this.http
      .post<any>(`${this.apiURL}/login`, credentials, {
        observe: 'response',
        withCredentials: true,
      })
      .pipe(
        tap((response) => {
          const jwt = response.headers.get('Authorization');
          if (jwt) {
            this.saveToken(jwt);
            this.setLoggedInStatus(true);
          }
        })
      );
  }

  logout() {
    this.loggedUser = undefined!;
    this.roles = undefined!;
    this.token = undefined!;
    this.isloggedIn = false;
    this.setLoggedInStatus(false);
    localStorage.removeItem('jwt');
    this.router.navigate(['/login']);
  }

  getInstallers(): Observable<any[]> {
    const token = this.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http
      .get<any[]>(`${this.apiURL}/list-installateurs`, { headers })
      .pipe(
        catchError((error) => {
          console.error('Error fetching installers:', error);
          throw new Error('Failed to load installers. Please try again.');
        })
      );
  }

  registerUser(user: User) {
    return this.http.post<User>(`${this.apiURL}/register`, user, {
      observe: 'response',
    });
  }

  validateEmail(code: string, email?: string): Observable<User> {
    let url = `${this.apiURL}/verifyEmail/${code}`;
    if (email) {
      url += `?email=${encodeURIComponent(email)}`;
    }
    return this.http.get<User>(url).pipe(
      tap((user) => {
        this.regitredUser = user;
        this.roles = user.roles;
      })
    );
  }

  setRegistredUser(user: User) {
    this.regitredUser = user;
  }

  getRegistredUser() {
    return this.regitredUser;
  }

  updateProfile(
username: string, newEmail?: string, newPassword?: string, currentPassword?: string, imagePath?: any  ): Observable<any> {
    const payload: any = { username };
    if (newEmail !== undefined) payload.newEmail = newEmail;
    if (newPassword !== undefined) payload.newPassword = newPassword;
    if (currentPassword !== undefined)
      payload.currentPassword = currentPassword;

    return this.http
      .put<any>(`${this.apiURL}/updateProfile`, payload, {
        headers: { Authorization: `Bearer ${this.getToken()}` },
      })
      .pipe(
        tap((response) => {
          Swal.fire('Succès', 'Profil mis à jour avec succès', 'success');
        }),
        catchError((error) => {
          Swal.fire(
            'Erreur',
            error.error.message || 'Une erreur est survenue',
            'error'
          );
          throw error;
        })
      );
  }

  getCurrentUserEmail(): Observable<string> {
    const decodedToken = this.jwtHelper.decodeToken(this.token);
    console.log('Email récupéré du token:', decodedToken?.email);
    return of(decodedToken?.email || '');
  }

  getUserProfile(): Observable<any> {
    console.log(
      'Envoi de la requête GET /userProfile avec token:',
      this.getToken()
    );
    return this.http.get<any>(`${this.apiURL}/userProfile`, {
      headers: { Authorization: `Bearer ${this.getToken()}` },
    });
  }

  getAllClients(): Observable<User[]> {
    const token = this.getToken();
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.get<User[]>(`${this.apiURL}/all`, { headers }).pipe(
      catchError((error) => {
        console.error('Error fetching clients:', error);
        throw new Error('Failed to load clients. Please try again.');
      })
    );
  }

  isClient(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('CLIENT');
  }

  isAdmin(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('ADMIN');
  }

  isInstaller(): boolean {
    const roles = this.getUserRoles();
    return roles.includes('INSTALLATEUR');
  }

  requestResetPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiURL}/request-reset-password`, { email });
  }

  validateCode(email: string, code: string): Observable<any> {
    return this.http.post(`${this.apiURL}/validate-code`, { email, code });
  }

  resetPassword(email: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.apiURL}/reset-password`, {
      email,
      newPassword,
    });
  }

  hasAnyRole(requiredRoles: string[]): boolean {
    const userRoles = this.getUserRoles();
    return requiredRoles.some((role) => userRoles.includes(role));
  }

  getRequiredRoles(): string[] {
    return this.getUserRoles();
  }


  // auth.service.ts
deactivateUser(userId: number): Observable<any> {
  const token = this.getToken();
  const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  return this.http.put(`${this.apiURL}/deactivate/${userId}`, {}, { headers });
}

activateUser(userId: number): Observable<any> {
  const token = this.getToken();
  const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  return this.http.put(`${this.apiURL}/activate/${userId}`, {}, { headers });
}
}
