import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Permission, PermissionRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class PermissionService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/permissions';

  getPermissions(resource?: string): Observable<ApiResponse<Permission[]>> {
    let params = new HttpParams();
    if (resource && resource.trim()) {
      params = params.set('resource', resource.trim());
    }
    return this.http.get<ApiResponse<Permission[]>>(this.API_URL, { params });
  }

  getPermissionById(id: number): Observable<ApiResponse<Permission>> {
    return this.http.get<ApiResponse<Permission>>(`${this.API_URL}/${id}`);
  }

  createPermission(request: PermissionRequest): Observable<ApiResponse<Permission>> {
    return this.http.post<ApiResponse<Permission>>(this.API_URL, request);
  }

  updatePermission(id: number, request: PermissionRequest): Observable<ApiResponse<Permission>> {
    return this.http.put<ApiResponse<Permission>>(`${this.API_URL}/${id}`, request);
  }

  deletePermission(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/${id}`);
  }
}
