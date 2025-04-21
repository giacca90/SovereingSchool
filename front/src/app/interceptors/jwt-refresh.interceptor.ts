import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { LoginService } from '../services/login.service';

export const jwtRefreshInterceptor: HttpInterceptorFn = (req, next) => {
	const loginService = inject(LoginService);
	return next(req).pipe(
		catchError((error: HttpErrorResponse) => {
			if (error.status === 401) {
				loginService
					.refreshToken()
					.then((token) => {
						if (token) {
							const clonedRequest = req.clone({
								setHeaders: {
									Authorization: `Bearer ${token}`,
								},
							});
							return next(clonedRequest);
						} else {
							loginService.usuario = null;
							loginService.id_usuario = null;
							localStorage.clear();
							console.error('No se pudo obtener un nuevo token');
							return Promise.reject('No se pudo obtener un nuevo token');
						}
					})
					.catch((e) => {
						console.error('Error refreshing token:', e);
					});
			}
			return throwError(() => error);
		}),
	);
};
