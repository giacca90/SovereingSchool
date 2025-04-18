import { isPlatformServer } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
	providedIn: 'root',
})
export class GuestGuard implements CanActivate {
	constructor(
		private router: Router,
		@Inject(PLATFORM_ID) private platformId: object,
	) {}

	canActivate(): boolean {
		if (isPlatformServer(this.platformId)) return false;
		if (localStorage.getItem('Usuario') === null) {
			this.router.navigate(['']);
			return false;
		}
		return true;
	}
}
