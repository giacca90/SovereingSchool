import { Component } from '@angular/core';
import { LoginModalServiceService } from '../../../services/login-modal-service.service';

@Component({
	selector: 'app-register',
	standalone: true,
	imports: [],
	templateUrl: './register.component.html',
	styleUrl: './register.component.css',
})
export class RegisterComponent {
	constructor(private modalService: LoginModalServiceService) {}

	close() {
		this.modalService.hide();
	}

	compruebaCorreo() {
		let message: HTMLDivElement = document.getElementById('message2') as HTMLDivElement;
		message.innerHTML = '';
		if (this.modalService.compruebaCorreo((document.getElementById('correo') as HTMLInputElement).value)) {
			let content: HTMLDivElement = document.getElementById('content2') as HTMLDivElement;
			content.innerHTML = `
				<br />
				<div id="message4" class="text-red-600"></div>
				<span class="m-4 rounded-lg border border-black p-1">Contraseña:</span>
				<br />
				<input type="password" id="password" class="m-4 rounded-lg border border-black p-1" placeholder="Contraseña" />
				<br />
				<span class="m-4 rounded-lg border border-black p-1">Repite la contraseña:</span>
				<br />
				<input type="password" id="password2" class="m-4 rounded-lg border border-black p-1" placeholder="Repite la contraseña" />
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
	compruebaPassword(): any {
		let pass: String = (document.getElementById('password') as HTMLInputElement).value;
		let pass2: String = (document.getElementById('password2') as HTMLInputElement).value;
		if (pass.length == 0 || pass2.length == 0) {
			(document.getElementById('message4') as HTMLParagraphElement).textContent = 'La contraseña no puede estas vacía';
			return;
		}
		if (pass === pass2) {
			console.log('password correcta!!!');
		} else {
			(document.getElementById('message4') as HTMLParagraphElement).textContent = 'Las dos contraseñas no coinciden';
		}
	}
}
