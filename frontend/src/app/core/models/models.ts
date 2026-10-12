export interface ApiResponse<T> {
  code: number;
  message?: string;
  data: T;
  timestamp?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  userId?: number;
  username?: string;
  email?: string;
  permissions?: string[];
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface Permission {
  id: number;
  name: string;
  description: string;
  resource: string;
  action: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PermissionRequest {
  name: string;
  description: string;
  resource: string;
  action: string;
}

export interface Role {
  id: number;
  name: string;
  description: string;
  isSystem?: boolean;
  permissions: Permission[];
  createdAt?: string;
  updatedAt?: string;
}

export interface RoleRequest {
  name: string;
  description: string;
  permissionIds?: number[];
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  enabled: boolean;
  roles: Role[];
  createdAt?: string;
  updatedAt?: string;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
  enabled?: boolean;
  roleIds?: number[];
}

export interface UserUpdateRequest {
  email: string;
  fullName?: string;
  enabled?: boolean;
  roleIds?: number[];
  newPassword?: string;
}

export interface CurrentUser {
  id?: number;
  username: string;
  email?: string;
  authorities: string[];
}
