import { AfterViewInit, Component } from '@angular/core';
import { Router } from '@angular/router';
import { Env } from '../../../environment';
import { Auth } from '../../models/Auth';
import { LoginModalService } from '../../services/login-modal.service';
import { LoginService } from '../../services/login.service';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';

@Component({
	selector: 'app-log-modal',
	standalone: true,
	templateUrl: './log-modal.component.html',
	styleUrl: './log-modal.component.css',
	imports: [LoginComponent, RegisterComponent],
})
export class LogModalComponent implements AfterViewInit {
	login: HTMLButtonElement | null = null;
	register: HTMLButtonElement | null = null;
	isLoginHidden: boolean = false;

	constructor(
		private modalService: LoginModalService,
		private loginService: LoginService,
		private router: Router,
	) {}

	ngAfterViewInit(): void {
		this.login = document.getElementById('login') as HTMLButtonElement;
		this.register = document.getElementById('register') as HTMLButtonElement;
	}

	clickLogin() {
		this.isLoginHidden = false;
		const isDarkMode = document.documentElement.classList.contains('dark');
		if (this.register?.classList.contains(isDarkMode ? 'dark:border-b-black' : 'bg-white')) {
			this.register.classList.remove(isDarkMode ? 'dark:border-b-black' : 'bg-white');
		}
		if (this.login?.classList.contains(isDarkMode ? 'dark:border-b-black' : 'bg-white') == false) {
			this.login.classList.add(isDarkMode ? 'dark:border-b-black' : 'bg-white');
		}
	}

	clickRegister() {
		this.isLoginHidden = true;
		const isDarkMode = document.documentElement.classList.contains('dark');

		if (this.login?.classList.contains(isDarkMode ? 'dark:border-b-black' : 'bg-white')) {
			this.login.classList.remove(isDarkMode ? 'dark:border-b-black' : 'bg-white');
		}
		if (this.register?.classList.contains(isDarkMode ? 'dark:border-b-black' : 'bg-white') == false) {
			this.register.classList.add(isDarkMode ? 'dark:border-b-black' : 'bg-white');
		}
	}

	close() {
		this.modalService.hide();
	}

	oauth2LoginWith(provider: string) {
		const width = 600;
		const height = 700;
		const left = (window.innerWidth - width) / 2 + window.screenX;
		const top = (window.innerHeight - height) / 2 + window.screenY;
		if (provider === 'google') {
			window.open(Env.BACK_BASE + '/oauth2/authorization/google', '_blank', `width=${width},height=${height},top=${top},left=${left}`);
		} else if (provider === 'github') {
			window.open(Env.BACK_BASE + '/oauth2/authorization/github', '_blank', `width=${width},height=${height},top=${top},left=${left}`);
		}

		const messageListener = (event: MessageEvent) => {
			if (event.origin !== Env.BACK_BASE) return;

			const authResponse: Auth = event.data;
			// Guarda el token en localStorage
			localStorage.setItem('Token', authResponse.accessToken);
			localStorage.setItem('Usuario', JSON.stringify(authResponse.usuario));
			this.loginService.usuario = authResponse.usuario;

			// Puedes emitir un evento o redirigir
			this.router.navigate(['']);
			this.modalService.hide();
			// Limpia el listener
			window.removeEventListener('message', messageListener);
		};

		window.addEventListener('message', messageListener);
	}
}
