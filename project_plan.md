# 🗺️ Kế hoạch dự án: User/Role/Permission Management

## Stack công nghệ

| Layer | Tech |
|---|---|
| Backend | Spring Boot 3.x + Spring Security 6 |
| Auth | JWT (Access Token + Refresh Token) |
| Database | PostgreSQL (hoặc MySQL) |
| ORM | Spring Data JPA + Hibernate |
| Frontend | Angular 17+ (Standalone Components) |
| UI Library | Angular Material hoặc PrimeNG |
| API Docs | Swagger / OpenAPI 3 |

---

## 📦 Phân chia module — 6 Phase

### Phase 1 — Thiết kế Database & Domain Model
> Mục tiêu: Xây dựng schema nền tảng

**Các bảng cần tạo:**
- `users` — thông tin tài khoản
- `roles` — nhóm quyền (ADMIN, MANAGER, USER...)
- `permissions` — quyền chi tiết (USER_READ, USER_CREATE, ROLE_DELETE...)
- `user_roles` — many-to-many: user ↔ role
- `role_permissions` — many-to-many: role ↔ permission

**ERD tóm tắt:**
```
User ──< user_roles >── Role ──< role_permissions >── Permission
```

**Output:** JPA Entities, Flyway/Liquibase migration scripts

---

### Phase 2 — Spring Boot Backend: Auth Module
> Mục tiêu: Đăng nhập, phát JWT, refresh token

**Các endpoint:**
- `POST /api/auth/login` — trả về access token + refresh token
- `POST /api/auth/refresh` — đổi refresh token lấy access token mới
- `POST /api/auth/logout` — thu hồi token

**Spring Security Config:**
- `SecurityFilterChain` — permit `/auth/**`, protect the rest
- `JwtAuthenticationFilter` — đọc token từ header `Authorization: Bearer ...`
- `UserDetailsService` — load user từ DB kèm permissions
- Dùng `@PreAuthorize("hasAuthority('USER_READ')")` trên từng API

**Output:** Auth flow hoàn chỉnh, JWT util class, Security config

---

### Phase 3 — Spring Boot Backend: Management APIs
> Mục tiêu: CRUD User, Role, Permission

**User APIs** (`/api/users`):
- `GET /` — danh sách user (cần `USER_READ`)
- `GET /{id}` — chi tiết user (cần `USER_READ`)
- `POST /` — tạo user (cần `USER_CREATE`)
- `PUT /{id}` — cập nhật user (cần `USER_UPDATE`)
- `DELETE /{id}` — xoá user (cần `USER_DELETE`)
- `POST /{id}/roles` — gán role cho user (cần `USER_ASSIGN_ROLE`)

**Role APIs** (`/api/roles`):
- CRUD tương tự (cần `ROLE_*` permissions)
- `POST /{id}/permissions` — gán permission cho role

**Permission APIs** (`/api/permissions`):
- `GET /` — list tất cả permissions (cần `PERMISSION_READ`)
- `POST /` — tạo permission mới (cần `PERMISSION_CREATE`)

**Output:** Controllers, Services, Repositories, DTOs, MapStruct mappers

---

### Phase 4 — Angular: Cấu trúc dự án & Auth Flow
> Mục tiêu: Setup Angular, login page, JWT interceptor

**Cấu trúc thư mục:**
```
src/
├── app/
│   ├── core/
│   │   ├── auth/           # AuthService, AuthGuard, JWT Interceptor
│   │   ├── guards/         # PermissionGuard
│   │   └── interceptors/   # HTTP interceptor thêm Bearer token
│   ├── shared/             # Components dùng chung (table, modal, badge...)
│   ├── features/
│   │   ├── auth/           # Login page
│   │   ├── users/          # Trang quản lý User
│   │   ├── roles/          # Trang quản lý Role
│   │   └── permissions/    # Trang quản lý Permission
│   └── layout/             # Sidebar, Header, Main layout
```

**Các tính năng:**
- Login form → gọi `/api/auth/login` → lưu token vào localStorage/memory
- `HttpInterceptor` tự động đính kèm `Authorization` header
- `AuthGuard` bảo vệ route, chuyển hướng về login nếu chưa đăng nhập
- `PermissionGuard` ẩn/hiện menu/button theo permission của user

**Output:** Login UI, routing setup, auth service, interceptors, guards

---

### Phase 5 — Angular: Management Screens
> Mục tiêu: Màn hình quản lý User, Role, Permission

**User Management Screen:**
- Table phân trang, search, filter
- Dialog tạo/sửa user (form validation)
- Gán role cho user (multi-select)
- Column status, action buttons (Edit, Delete) — ẩn theo permission

**Role Management Screen:**
- Table danh sách role
- Dialog tạo/sửa role
- Gán permissions cho role (checkbox tree hoặc multi-select)

**Permission Management Screen:**
- Table danh sách permission
- Tạo/sửa permission (name, description, resource, action)

**Output:** 3 feature modules hoàn chỉnh với UI đẹp

---

### Phase 6 — Polish & Security Hardening
> Mục tiêu: Hoàn thiện, bảo mật, tài liệu

**Backend:**
- Global exception handler (`@ControllerAdvice`)
- Rate limiting (Spring + Bucket4j)
- Swagger UI với auth (lock icon)
- Unit tests cho Service layer
- `@PreAuthorize` kiểm tra đầy đủ

**Frontend:**
- Refresh token tự động khi 401
- Loading spinner toàn cục
- Toast notifications
- Responsive layout

**Output:** Production-ready codebase

---

## 🗓️ Thứ tự thực hiện gợi ý

```
Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6
  DB          Auth     APIs       Angular    UI          Polish
  (1 ngày)  (2 ngày)  (2 ngày)  (1 ngày)  (3 ngày)   (1 ngày)
```

---

## ❓ Cần xác nhận trước khi bắt đầu

1. **Database**: PostgreSQL hay MySQL?
2. **Build tool**: Maven hay Gradle?
3. **Angular UI**: Angular Material hay PrimeNG hay tự viết CSS?
4. **Bắt đầu từ Phase nào?** (Khuyến nghị từ Phase 1)
5. **Cấu trúc mono-repo hay 2 repo riêng** (backend + frontend)?
