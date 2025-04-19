import { isPlatformBrowser, isPlatformServer } from '@angular/common';
import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
	const platformId = inject(PLATFORM_ID);

	if (isPlatformBrowser(platformId)) {
		// 🌐 En el navegador → usar token del localStorage o visitante
		const token = localStorage.getItem('Token');
		const headers = token ? { Authorization: `Bearer ${token}` } : { Authorization: `Basic ${btoa('Visitante:visitante')}` };

		req = req.clone({ setHeaders: headers });
	}

	if (isPlatformServer(platformId)) {
		// 🖥️ En SSR → usar siempre las credenciales fijas codificadas
		const credentials = Buffer.from('Visitante:visitante').toString('base64');
		req = req.clone({
			setHeaders: {
				Authorization: `Basic ${credentials}`,
			},
		});
	}

	return next(req);
};
