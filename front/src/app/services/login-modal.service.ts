import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class LoginModalService {
	private isVisible = new BehaviorSubject<boolean>(false);
	isVisible$ = this.isVisible.asObservable();

	show() {
		this.isVisible.next(true);
	}

	hide() {
		this.isVisible.next(false);
	}

	compruebaCorreo(correo: String): Boolean {
		return true;
	}

	compruebaPassword(password: String): Boolean {
		return false;
	}
}
