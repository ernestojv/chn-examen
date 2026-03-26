import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest } from '../interfaces/auth.interface';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly apiUrl = `${environment.apiBaseUrl}/auth`;
  private readonly tokenKey = 'auth_token';

  /** Sends login credentials to the backend and stores the returned JWT token. */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        this.storeToken(response.token);
        this.saveUsername(credentials.username);
      })
    );
  }

  /** Retrieves the stored JWT token from localStorage. */
  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(this.tokenKey);
  }

  /** Returns true if a token is currently stored. */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = this.parseJwtPayload(token);
      const exp = payload['exp'];
      const expiresAt = typeof exp === 'number' ? exp * 1000 : null;

      if (expiresAt && Date.now() >= expiresAt) {
        this.logout();
        return false;
      }

      return true;
    } catch {
      this.logout();
      return false;
    }
  }

  /** Removes the stored token (logout). */
  logout(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem('auth_user');
  }

  getUsername(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    
    // 1. Try from localStorage (set during latest login)
    const stored = localStorage.getItem('auth_user');
    if (stored) return stored;

    // 2. Fallback: Parse from JWT token if exists
    const token = this.getToken();
    if (!token) return null;

    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      const payload = JSON.parse(jsonPayload);
      return payload.sub || null; // In Spring Boot subject is usually the username
    } catch (e) {
      return null;
    }
  }

  private saveUsername(username: string): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem('auth_user', username);
  }

  private storeToken(token: string): void {
    if (!isPlatformBrowser(this.platformId)) return;
    localStorage.setItem(this.tokenKey, token);
  }

  private parseJwtPayload(token: string): Record<string, unknown> {
    const base64Url = token.split('.')[1];
    if (!base64Url) throw new Error('Invalid JWT structure');

    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );

    const parsed = JSON.parse(jsonPayload);
    if (!parsed || typeof parsed !== 'object') {
      throw new Error('Invalid JWT payload');
    }

    return parsed as Record<string, unknown>;
  }
}
