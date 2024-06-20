import { Component } from '@angular/core';
import { LoginModalServiceService } from '../../../services/login-modal-service.service';

@Component({
	selector: 'app-login',
	standalone: true,
	imports: [],
	templateUrl: './login.component.html',
	styleUrl: './login.component.css',
})
export class LoginComponent {
	constructor(private modalService: LoginModalServiceService) {}

	close() {
		this.modalService.hide();
	}

	compruebaCorreo() {
		let message: HTMLDivElement = document.getElementById('message') as HTMLDivElement;
		message.innerHTML = '';
		if (this.modalService.compruebaCorreo((document.getElementById('correo') as HTMLInputElement).value)) {
			let content: HTMLDivElement = document.getElementById('content') as HTMLDivElement;
			content.innerHTML = `
				<br />
				<div id="message3" class="text-red-600"></div>
				<span class="m-4 rounded-lg border border-black p-1">Contraseña:</span>
				<br />
				<input type="password" id="password" class="m-4 rounded-lg border border-black p-1" placeholder="Password" />
				<br />
				<button id="nextButton2" class="m-4 rounded-lg border border-black bg-green-300 p-1" (click)="compruebaCorreo()">Siguiente</button>
				<button id="cancelButton2" class="m-4 rounded-lg border border-black bg-red-300 p-1" (click)="close()">Cancelar</button>
			`;
			const nextButton = document.getElementById('nextButton2') as HTMLButtonElement;
			const cancelButton = document.getElementById('cancelButton2') as HTMLButtonElement;

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

	compruebaPassword(): any {
		let message3: HTMLDivElement = document.getElementById('message3') as HTMLDivElement;
		message3.innerHTML = '';
		if (this.modalService.compruebaPassword((document.getElementById('password') as HTMLInputElement).value)) {
		} else {
			let mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'La contraseña no es correcta!!!';
			message3.appendChild(mex);
		}
	}
}
