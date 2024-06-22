import { Component } from '@angular/core';
import { LoginModalService } from '../../../services/login-modal.service';
import { LoginService } from '../../../services/login.service';

@Component({
	selector: 'app-login',
	standalone: true,
	imports: [],
	templateUrl: './login.component.html',
	styleUrl: './login.component.css',
})
export class LoginComponent {
	constructor(
		private modalService: LoginModalService,
		private loginService: LoginService,
	) {}

	close() {
		this.modalService.hide();
	}

	async compruebaCorreo() {
		let message: HTMLDivElement = document.getElementById('message') as HTMLDivElement;
		message.innerHTML = '';
		let correo: string = (document.getElementById('correo') as HTMLInputElement).value;
		if (correo.length == 0) {
			let mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'El correo electrónico no puede estar vació!!!';
			message.appendChild(mex);
			return;
		}

		if (!this.compruebaEmail(correo)) {
			let mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'El correo electrónico no tiene un formato valido!!!';
			message.appendChild(mex);
			return;
		}
		if ((await this.loginService.compruebaCorreo(correo)) === true) {
			let content: HTMLDivElement = document.getElementById('content') as HTMLDivElement;
			content.innerHTML = `
				<br />
				<div id="message3" class="text-red-600 ml-4 mr-4"></div>
				<span class="m-4 rounded-lg p-1">Contraseña:</span>
				<br />
				<input type="password" id="password" class="m-4 rounded-lg border border-black p-1" placeholder="Password" />
				<br />
				<button id="nextButton" class="m-4 rounded-lg border border-black bg-green-300 p-1" (click)="compruebaCorreo()">Siguiente</button>
				<button id="cancelButton" class="m-4 rounded-lg border border-black bg-red-300 p-1" (click)="close()">Cancelar</button>
			`;
			const nextButton = document.getElementById('nextButton') as HTMLButtonElement;
			const cancelButton = document.getElementById('cancelButton') as HTMLButtonElement;

			if (nextButton) {
				nextButton.addEventListener('click', () => this.compruebaPassword());
			}

			if (cancelButton) {
				cancelButton.addEventListener('click', () => this.close());
			}
		} else {
			let mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'Este correo no está registrado!!!';
			message.appendChild(mex);
		}
	}

	async compruebaPassword() {
		let message3: HTMLDivElement = document.getElementById('message3') as HTMLDivElement;
		message3.innerHTML = '';
		if (await this.loginService.compruebaPassword((document.getElementById('password') as HTMLInputElement).value)) {
			this.close();
		} else {
			let mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'La contraseña no es correcta!!!';
			message3.appendChild(mex);
			return;
		}
	}

	compruebaEmail(email: string): boolean {
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		return emailRegex.test(email);
	}
}
