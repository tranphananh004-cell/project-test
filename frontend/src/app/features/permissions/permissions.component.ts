import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PermissionService } from '../../core/services/permission.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { Permission, PermissionRequest } from '../../core/models/models';

@Component({
  selector: 'app-permissions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './permissions.component.html'
})
export class PermissionsComponent implements OnInit {
  permissionService = inject(PermissionService);
  authService = inject(AuthService);
  toastService = inject(ToastService);

  permissions: Permission[] = [];
  filteredPermissions: Permission[] = [];
  activeTab = '';
  loading = false;
  submitting = false;

  showFormModal = false;
  isEditMode = false;
  showDeleteModal = false;
  selectedPerm: Permission | null = null;

  formPerm: PermissionRequest = {
    name: '',
    resource: '',
    action: 'READ',
    description: ''
  };

  ngOnInit() {
    this.loadPermissions();
  }

  loadPermissions() {
    this.loading = true;
    this.permissionService.getPermissions().subscribe({
      next: res => {
        this.loading = false;
        this.permissions = res.data || [];
        this.filterByResource(this.activeTab);
      },
      error: () => {
        this.loading = false;
        this.toastService.error('Không thể tải danh sách quyền');
      }
    });
  }

  filterByResource(res: string) {
    this.activeTab = res;
    if (!res) {
      this.filteredPermissions = this.permissions;
    } else {
      this.filteredPermissions = this.permissions.filter(p => p.resource.toLowerCase() === res.toLowerCase());
    }
  }

  getActionBadgeClass(action: string): string {
    switch (action.toUpperCase()) {
      case 'READ': return 'badge-info';
      case 'CREATE': return 'badge-success';
      case 'UPDATE': return 'badge-warning';
      case 'DELETE': return 'badge-danger';
      default: return 'badge-primary';
    }
  }

  openCreateModal() {
    this.isEditMode = false;
    this.formPerm = { name: '', resource: '', action: 'READ', description: '' };
    this.showFormModal = true;
  }

  openEditModal(p: Permission) {
    this.isEditMode = true;
    this.selectedPerm = p;
    this.formPerm = {
      name: p.name,
      resource: p.resource,
      action: p.action,
      description: p.description || ''
    };
    this.showFormModal = true;
  }

  savePermission() {
    this.submitting = true;
    if (this.isEditMode && this.selectedPerm) {
      this.permissionService.updatePermission(this.selectedPerm.id, this.formPerm).subscribe({
        next: () => {
          this.submitting = false;
          this.showFormModal = false;
          this.toastService.success('Cập nhật permission thành công');
          this.loadPermissions();
        },
        error: err => {
          this.submitting = false;
          this.toastService.error(err.error?.message || 'Lỗi cập nhật permission');
        }
      });
    } else {
      this.permissionService.createPermission(this.formPerm).subscribe({
        next: () => {
          this.submitting = false;
          this.showFormModal = false;
          this.toastService.success('Tạo permission thành công');
          this.loadPermissions();
        },
        error: err => {
          this.submitting = false;
          this.toastService.error(err.error?.message || 'Lỗi tạo permission');
        }
      });
    }
  }

  openDeleteModal(p: Permission) {
    this.selectedPerm = p;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (!this.selectedPerm) return;
    this.submitting = true;
    this.permissionService.deletePermission(this.selectedPerm.id).subscribe({
      next: () => {
        this.submitting = false;
        this.showDeleteModal = false;
        this.toastService.success(`Đã xoá permission ${this.selectedPerm?.name}`);
        this.loadPermissions();
      },
      error: err => {
        this.submitting = false;
        this.toastService.error(err.error?.message || 'Không thể xoá permission');
      }
    });
  }
}
