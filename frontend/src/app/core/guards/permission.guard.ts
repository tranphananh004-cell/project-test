import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const permissionGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toastService = inject(ToastService);

  const requiredPermission = route.data?.['permission'] as string | undefined;

  if (!requiredPermission || authService.hasPermission(requiredPermission)) {
    return true;
  }

  toastService.error(`Bạn không có quyền truy cập trang này (${requiredPermission})`, 'Từ chối truy cập');
  router.navigate(['/']);
  return false;
};
