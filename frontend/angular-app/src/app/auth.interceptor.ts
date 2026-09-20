import { HttpInterceptorFn } from '@angular/common/http';
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = localStorage.getItem('accessToken');
  const headers: Record<string, string> = {};
  if (!request.headers.has('X-Correlation-ID')) headers['X-Correlation-ID'] = crypto.randomUUID();
  const isAuthEndpoint = request.url.includes('/auth/login') || request.url.includes('/auth/register');
  if (token && !isAuthEndpoint) headers['Authorization'] = `Bearer ${token}`;
  return next(request.clone({ setHeaders: headers }));
};
