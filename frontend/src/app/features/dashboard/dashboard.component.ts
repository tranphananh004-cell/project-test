import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { RoleService } from '../../core/services/role.service';
import { PermissionService } from '../../core/services/permission.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  authService = inject(AuthService);
  userService = inject(UserService);
  roleService = inject(RoleService);
  permissionService = inject(PermissionService);

  userCount = 0;
  roleCount = 0;
  permissionCount = 0;

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    if (this.authService.hasPermission('USER_READ')) {
      this.userService.getUsers('', 0, 1).subscribe({
        next: res => this.userCount = res.data?.totalElements || 0,
        error: () => {}
      });
    }

    if (this.authService.hasPermission('ROLE_READ')) {
      this.roleService.getRoles().subscribe({
        next: res => this.roleCount = res.data?.length || 0,
        error: () => {}
      });
    }

    if (this.authService.hasPermission('PERMISSION_READ')) {
      this.permissionService.getPermissions().subscribe({
        next: res => this.permissionCount = res.data?.length || 0,
        error: () => {}
      });
    }
  }
}
