import { AfterViewInit, Component } from '@angular/core';
import { LoginModalServiceService } from '../../services/login-modal-service.service';
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

	constructor(private modalService: LoginModalServiceService) {}

	ngAfterViewInit(): void {
		this.login = document.getElementById('login') as HTMLButtonElement;
		this.register = document.getElementById('register') as HTMLButtonElement;
	}

	clickLogin() {
		this.isLoginHidden = false;
		if (this.register?.classList.contains('border-b-white')) {
			this.register.classList.remove('border-b-white');
		}
		if (this.login?.classList.contains('border-b-white') == false) {
			this.login.classList.add('border-b-white');
		}
	}

	clickRegister() {
		this.isLoginHidden = true;
		if (this.login?.classList.contains('border-b-white')) {
			this.login.classList.remove('border-b-white');
		}
		if (this.register?.classList.contains('border-b-white') == false) {
			this.register.classList.add('border-b-white');
		}
	}

	close() {
		this.modalService.hide();
	}
}
