import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Role, RoleRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/roles';

  getRoles(): Observable<ApiResponse<Role[]>> {
    return this.http.get<ApiResponse<Role[]>>(this.API_URL);
  }

  getRoleById(id: number): Observable<ApiResponse<Role>> {
    return this.http.get<ApiResponse<Role>>(`${this.API_URL}/${id}`);
  }

  createRole(request: RoleRequest): Observable<ApiResponse<Role>> {
    return this.http.post<ApiResponse<Role>>(this.API_URL, request);
  }

  updateRole(id: number, request: RoleRequest): Observable<ApiResponse<Role>> {
    return this.http.put<ApiResponse<Role>>(`${this.API_URL}/${id}`, request);
  }

  assignPermissions(id: number, permissionIds: number[]): Observable<ApiResponse<Role>> {
    return this.http.put<ApiResponse<Role>>(`${this.API_URL}/${id}/permissions`, { permissionIds });
  }

  deleteRole(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/${id}`);
  }
}
