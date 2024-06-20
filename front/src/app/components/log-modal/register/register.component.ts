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
		let correo: string = (document.getElementById('correo2') as HTMLInputElement).value;

		if ((document.getElementById('nombre2') as HTMLInputElement).value.length == 0) {
			let mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'El nombre no puede estas vacío.';
			message.appendChild(mex);
			return;
		}
		if (correo.length == 0) {
			let mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'No has puesto el Correo!!!';
			message.appendChild(mex);
			return;
		}

		if (!this.compruebaEmail(correo)) {
			let mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'El correo electrónico no tiene un formato valido!!!';
			message.appendChild(mex);
			return;
		}

		if (this.modalService.compruebaCorreo(correo)) {
			let content: HTMLDivElement = document.getElementById('content2') as HTMLDivElement;
			content.innerHTML = `
				<br />
				<div id="message4" class="text-red-600 ml-4 mr-4"></div>
				<span class="m-4 rounded-lg p-1">Contraseña:</span>
				<br />
				<input type="password" id="password" class="m-4 rounded-lg border border-black p-1" placeholder="Contraseña" />
				<br />
				<span class="m-4 rounded-lg p-1">Repite la contraseña:</span>
				<br />
				<input type="password" id="password2" class="m-4 rounded-lg border border-black p-1" placeholder="Repite la contraseña" />
				<br />
				<button id="nextButton2" class="m-4 rounded-lg border border-black bg-green-300 p-1" >Siguiente</button>
				<button id="cancelButton2" class="m-4 rounded-lg border border-black bg-red-300 p-1" >Cancelar</button>
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
			return;
		}
	}
	compruebaPassword(): any {
		let message: HTMLDivElement = document.getElementById('message4') as HTMLDivElement;
		message.innerHTML = '';
		let pass: String = (document.getElementById('password') as HTMLInputElement).value;
		let pass2: String = (document.getElementById('password2') as HTMLInputElement).value;
		if (pass.length === 0) {
			message.textContent = 'La contraseña no puede estas vacía';
			return;
		}
		if (pass2.length === 0) {
			message.textContent = 'La contraseña no puede estas vacía';
			return;
		}
		if (pass == pass2) {
			console.log('Contraseña correcta!!!');
			return;
		} else {
			message.textContent = 'Las dos contraseñas no coinciden';
			return;
		}
	}

	compruebaEmail(email: string): boolean {
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		return emailRegex.test(email);
	}
}
