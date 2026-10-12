import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();

  // Kiểm tra endpoint xác thực (login/refresh) — không gắn token
  const isLoginEndpoint   = req.url.includes('/auth/login');
  const isRefreshEndpoint = req.url.includes('/auth/refresh');
  const isAuthEndpoint    = isLoginEndpoint || isRefreshEndpoint;

  let authReq = req;
  if (token && !isAuthEndpoint) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Chỉ tự động refresh khi:
      // 1. Lỗi 401
      // 2. KHÔNG phải login endpoint (login sai mật khẩu không được retry)
      // 3. KHÔNG phải refresh endpoint (tránh vòng lặp vô tận)
      // 4. Vẫn còn refresh token trong storage
      if (
        error.status === 401 &&
        !isLoginEndpoint &&
        !isRefreshEndpoint &&
        authService.getRefreshToken()
      ) {
        return authService.refreshToken().pipe(
          switchMap(res => {
            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${res.accessToken}`
              }
            });
            return next(retryReq);
          }),
          catchError(refreshErr => {
            authService.logout();
            return throwError(() => refreshErr);
          })
        );
      }
      // Với login sai mật khẩu (401 từ /auth/login), trả lỗi thẳng về component
      return throwError(() => error);
    })
  );
};
