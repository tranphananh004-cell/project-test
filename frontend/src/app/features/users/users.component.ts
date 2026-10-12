import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { RoleService } from '../../core/services/role.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { User, Role } from '../../core/models/models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  userService = inject(UserService);
  roleService = inject(RoleService);
  authService = inject(AuthService);
  toastService = inject(ToastService);

  users: User[] = [];
  availableRoles: Role[] = [];
  keyword = '';
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  totalPages = 0;
  isLastPage = false;
  loading = false;
  submitting = false;

  // Modals state
  showFormModal = false;
  isEditMode = false;
  showRoleModal = false;
  showDeleteModal = false;
  selectedUser: User | null = null;
  selectedRoleIds: number[] = [];

  formUser: any = {
    username: '',
    email: '',
    fullName: '',
    password: ''
  };

  ngOnInit() {
    this.loadUsers();
    this.loadRoles();
  }

  loadUsers(page = 0) {
    this.currentPage = page;
    this.loading = true;
    this.userService.getUsers(this.keyword, this.currentPage, this.pageSize).subscribe({
      next: res => {
        this.loading = false;
        if (res.data) {
          this.users = res.data.content || [];
          this.totalElements = res.data.totalElements;
          this.totalPages = res.data.totalPages;
          this.isLastPage = res.data.last;
        }
      },
      error: () => {
        this.loading = false;
        this.toastService.error('Không thể tải danh sách người dùng');
      }
    });
  }

  loadRoles() {
    this.roleService.getRoles().subscribe({
      next: res => this.availableRoles = res.data || [],
      error: () => {}
    });
  }

  


  openCreateModal() {
    this.isEditMode = false;
    this.formUser = { username: '', email: '', fullName: '', password: '' };
    this.selectedRoleIds = [];
    this.showFormModal = true;
  }

  openEditModal(user: User) {
    this.isEditMode = true;
    this.selectedUser = user;
    this.formUser = {
      email: user.email,
      fullName: user.fullName || '',
      password: ''
    };
    this.showFormModal = true;
  }

  closeFormModal() {
    this.showFormModal = false;
    this.selectedUser = null;
  }

  toggleRoleSelect(roleId: number) {
    if (this.selectedRoleIds.includes(roleId)) {
      this.selectedRoleIds = this.selectedRoleIds.filter(id => id !== roleId);
    } else {
      this.selectedRoleIds.push(roleId);
    }
  }

  saveUser() {
    this.submitting = true;
    if (this.isEditMode && this.selectedUser) {
      this.userService.updateUser(this.selectedUser.id, {
        email: this.formUser.email,
        fullName: this.formUser.fullName,
        newPassword: this.formUser.password || undefined
      }).subscribe({
        next: () => {
          this.submitting = false;
          this.closeFormModal();
          this.toastService.success('Cập nhật người dùng thành công');
          this.loadUsers(this.currentPage);
        },
        error: err => {
          this.submitting = false;
          this.toastService.error(err.error?.message || 'Lỗi cập nhật người dùng');
        }
      });
    } else {
      this.userService.createUser({
        username: this.formUser.username,
        email: this.formUser.email,
        fullName: this.formUser.fullName,
        password: this.formUser.password,
        roleIds: this.selectedRoleIds
      }).subscribe({
        next: () => {
          this.submitting = false;
          this.closeFormModal();
          this.toastService.success('Tạo người dùng mới thành công');
          this.loadUsers(0);
        },
        error: err => {
          this.submitting = false;
          this.toastService.error(err.error?.message || 'Lỗi tạo người dùng');
        }
      });
    }
  }

  openAssignRoleModal(user: User) {
    this.selectedUser = user;
    this.selectedRoleIds = user.roles ? user.roles.map(r => r.id) : [];
    this.showRoleModal = true;
  }

  saveAssignedRoles() {
    if (!this.selectedUser) return;
    this.submitting = true;
    this.userService.assignRoles(this.selectedUser.id, this.selectedRoleIds).subscribe({
      next: () => {
        this.submitting = false;
        this.showRoleModal = false;
        this.toastService.success(`Đã cập nhật vai trò cho @${this.selectedUser?.username}`);
        this.loadUsers(this.currentPage);
      },
      error: err => {
        this.submitting = false;
        this.toastService.error(err.error?.message || 'Lỗi gán vai trò');
      }
    });
  }

  openDeleteModal(user: User) {
    this.selectedUser = user;
    this.showDeleteModal = true;
  }

  confirmDelete() {
    if (!this.selectedUser) return;
    this.submitting = true;
    this.userService.deleteUser(this.selectedUser.id).subscribe({
      next: () => {
        this.submitting = false;
        this.showDeleteModal = false;
        this.toastService.success(`Đã xoá người dùng @${this.selectedUser?.username}`);
        this.loadUsers(this.currentPage);
      },
      error: err => {
        this.submitting = false;
        this.toastService.error(err.error?.message || 'Lỗi xoá người dùng');
      }
    });
  }
}
