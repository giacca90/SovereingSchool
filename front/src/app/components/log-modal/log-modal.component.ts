import { AfterViewInit, Component } from '@angular/core';
import { LoginModalService } from '../../services/login-modal.service';
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

	constructor(private modalService: LoginModalService) {}

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
}
