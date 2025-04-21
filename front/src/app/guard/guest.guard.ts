import { isPlatformServer } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { LoginService } from '../services/login.service';

@Injectable({
	providedIn: 'root',
})
export class GuestGuard implements CanActivate {
	constructor(
		private router: Router,
		private loginService: LoginService,
		@Inject(PLATFORM_ID) private platformId: object,
	) {}

	canActivate(): boolean {
		if (isPlatformServer(this.platformId)) return false;
		if (!this.loginService.usuario) {
			this.router.navigate(['']);
			return false;
		}
		return true;
	}
}
