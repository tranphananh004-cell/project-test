import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RoleService } from '../../core/services/role.service';
import { PermissionService } from '../../core/services/permission.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { Role, Permission } from '../../core/models/models';

@Component({
  selector: 'app-roles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './roles.component.html'
})
export class RolesComponent implements OnInit {
  roleService = inject(RoleService);
  permissionService = inject(PermissionService);
  authService = inject(AuthService);
  toastService = inject(ToastService);

  roles: Role[] = [];
  allPermissions: Permission[] = [];
  loading = false;
  submitting = false;

  showFormModal = false;
  isEditMode = false;
  showPermissionModal = false;
  showDeleteModal = false;
  selectedRole: Role | null = null;
  selectedPermIds: number[] = [];

  formRole = { name: '', description: '' };

  ngOnInit() {
    this.loadRoles();
    this.loadPermissions();
  }

  loadRoles() {
    this.loading = true;
    this.roleService.getRoles().subscribe({
      next: res => {
        this.loading = false;
        this.roles = res.data || [];
      },
      error: () => {
        this.loading = false;
        this.toastService.error('Không thể tải danh sách vai trò');
      }
    });
  }

  loadPermissions() {
    this.permissionService.getPermissions().subscribe({
      next: res => this.allPermissions = res.data || [],
      error: () => {}
    });
  }

  openCreateModal() {
    this.isEditMode = false;
    this.formRole = { name: '', description: '' };
    this.showFormModal = true;
  }

  openEditModal(role: Role) {
    this.isEditMode = true;
    this.selectedRole = role;
    this.formRole = { name: role.name, description: role.description || '' };
    this.showFormModal = true;
  }

  saveRole() {
    this.submitting = true;
    if (this.isEditMode && this.selectedRole) {
      this.roleService.updateRole(this.selectedRole.id, this.formRole).subscribe({
        next: () => {
          this.submitting = false;
          this.showFormModal = false;
          this.toastService.success('Cập nhật vai trò thành công');
          this.loadRoles();
        },
        error: err => {
          this.submitting = false;
          this.toastService.error(err.error?.message || 'Lỗi cập nhật vai trò');
        }
      });
    } else {
      this.roleService.createRole(this.formRole).subscribe({
        next: () => {
          this.submitting = false;
          this.showFormModal = false;
          this.toastService.success('Tạo vai trò thành công');
          this.loadRoles();
        },
        error: err => {
          this.submitting = false;
          this.toastService.error(err.error?.message || 'Lỗi tạo vai trò');
        }
      });
    }
  }

  openAssignPermissionModal(role: Role) {
    this.selectedRole = role;
    this.selectedPermIds = role.permissions ? role.permissions.map(p => p.id) : [];
    this.showPermissionModal = true;
  }

  getResourceList(): string[] {
    const set = new Set<string>();
    this.allPermissions.forEach(p => set.add(p.resource));
    return Array.from(set);
  }

  getPermsByResource(res: string): Permission[] {
    return this.allPermissions.filter(p => p.resource === res);
  }

  togglePermSelect(permId: number) {
    if (this.selectedPermIds.includes(permId)) {
      this.selectedPermIds = this.selectedPermIds.filter(id => id !== permId);
    } else {
      this.selectedPermIds.push(permId);
    }
  }

  selectAllPerms() {
    this.selectedPermIds = this.allPermissions.map(p => p.id);
  }

  clearAllPerms() {
    this.selectedPermIds = [];
  }

  saveAssignedPermissions() {
    if (!this.selectedRole) return;
    this.submitting = true;
    this.roleService.assignPermissions(this.selectedRole.id, this.selectedPermIds).subscribe({
      next: () => {
        this.submitting = false;
        this.showPermissionModal = false;
        this.toastService.success(`Đã cập nhật quyền hạn cho vai trò ${this.selectedRole?.name}`);
        this.loadRoles();
      },
      error: err => {
        this.submitting = false;
        this.toastService.error(err.error?.message || 'Lỗi gán quyền');
      }
    });
  }

  openDeleteModal(role: Role) {
    this.selectedRole = role;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (!this.selectedRole) return;
    this.submitting = true;
    this.roleService.deleteRole(this.selectedRole.id).subscribe({
      next: () => {
        this.submitting = false;
        this.showDeleteModal = false;
        this.toastService.success(`Đã xoá vai trò ${this.selectedRole?.name}`);
        this.loadRoles();
      },
      error: err => {
        this.submitting = false;
        this.toastService.error(err.error?.message || 'Không thể xoá vai trò');
      }
    });
  }
}
