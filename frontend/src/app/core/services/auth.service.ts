import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { CurrentUser, LoginRequest, LoginResponse, RefreshTokenResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API_URL = 'http://localhost:8080/api';
  private readonly ACCESS_TOKEN_KEY = 'nexus_access_token';
  private readonly REFRESH_TOKEN_KEY = 'nexus_refresh_token';
  private readonly PERMISSIONS_KEY = 'nexus_permissions';

  currentUserSignal = signal<CurrentUser | null>(this.getStoredUser());
  isAuthenticated = computed(() => !!this.currentUserSignal());

  constructor() {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/auth/login`, credentials).pipe(
      tap(res => {
        this.saveTokens(res.accessToken, res.refreshToken);
        if (res.permissions && res.permissions.length > 0) {
          localStorage.setItem(this.PERMISSIONS_KEY, JSON.stringify(res.permissions));
        }
        const user = this.decodeToken(res.accessToken, res.permissions);
        this.currentUserSignal.set(user);
      })
    );
  }

  refreshToken(): Observable<RefreshTokenResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No refresh token'));
    }

    return this.http.post<RefreshTokenResponse>(`${this.API_URL}/auth/refresh`, { refreshToken }).pipe(
      tap(res => {
        this.saveTokens(res.accessToken, res.refreshToken);
        const user = this.decodeToken(res.accessToken);
        this.currentUserSignal.set(user);
      }),
      catchError(err => {
        this.logout();
        return throwError(() => err);
      })
    );
  }

  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post(`${this.API_URL}/auth/logout`, { refreshToken }).subscribe({
        error: () => {} // ignore error on logout
      });
    }

    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.PERMISSIONS_KEY);
    this.currentUserSignal.set(null);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  hasPermission(permission: string): boolean {
    const user = this.currentUserSignal();
    if (!user || !user.authorities) return false;
    return user.authorities.includes(permission);
  }

  hasAnyPermission(permissions: string[]): boolean {
    return permissions.some(p => this.hasPermission(p));
  }

  private saveTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  private decodeToken(token: string, explicitPermissions?: string[]): CurrentUser | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = JSON.parse(atob(parts[1]));

      let authorities: string[] = [];
      if (explicitPermissions && explicitPermissions.length > 0) {
        authorities = explicitPermissions;
      } else if (Array.isArray(payload.authorities)) {
        authorities = payload.authorities;
      } else if (Array.isArray(payload.roles)) {
        authorities = payload.roles;
      } else {
        const stored = localStorage.getItem(this.PERMISSIONS_KEY);
        if (stored) {
          try {
            authorities = JSON.parse(stored);
          } catch {}
        }
      }

      return {
        id: payload.userId,
        username: payload.sub || 'User',
        authorities
      };
    } catch {
      return null;
    }
  }

  private getStoredUser(): CurrentUser | null {
    const token = this.getAccessToken();
    if (!token) return null;
    return this.decodeToken(token);
  }
}
