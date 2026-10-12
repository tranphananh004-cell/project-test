import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  authService = inject(AuthService);
  toastService = inject(ToastService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  cdr = inject(ChangeDetectorRef);

  username = '';
  password = '';
  showPassword = false;
  loading = false;
  activeDemoRole = '';

  fillDemo(u: string, p: string, roleName: string = 'Super Admin') {
    this.username = u;
    this.password = p;
    this.activeDemoRole = roleName;
  }

  onSubmit() {
    if (!this.username || !this.password) return;

    this.loading = true;
    this.cdr.markForCheck();

    this.authService.login({ username: this.username, password: this.password })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.toastService.success('Đăng nhập thành công! Chào mừng bạn.', 'Xin chào');
          const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
          this.router.navigateByUrl(returnUrl);
        },
        error: err => {
          const msg = err.error?.message || 'Tài khoản hoặc mật khẩu không đúng!';
          this.toastService.error(msg, 'Đăng nhập thất bại');
        }
      });
  }
}
