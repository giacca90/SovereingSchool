import { Component, OnDestroy } from '@angular/core';
import { NuevoUsuario } from '../../../models/NuevoUsuario';
import { LoginModalService } from '../../../services/login-modal.service';
import { LoginService } from '../../../services/login.service';
import { RegisterService } from '../../../services/register.service';

@Component({
	selector: 'app-register',
	standalone: true,
	imports: [],
	templateUrl: './register.component.html',
	styleUrl: './register.component.css',
})
export class RegisterComponent implements OnDestroy {
	private nuevoUsuario: NuevoUsuario = new NuevoUsuario();

	constructor(
		private modalService: LoginModalService,
		private loginService: LoginService,
		private registerService: RegisterService,
	) {}

	ngOnDestroy(): void {
		this.nuevoUsuario = new NuevoUsuario();
	}

	close() {
		this.modalService.hide();
	}

	async compruebaCorreo() {
		const message: HTMLDivElement = document.getElementById('message2') as HTMLDivElement;
		message.innerHTML = '';
		const correo: string = (document.getElementById('correo2') as HTMLInputElement).value;

		if ((document.getElementById('nombre2') as HTMLInputElement).value.length == 0) {
			const mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'El nombre no puede estas vacío.';
			message.appendChild(mex);
			return;
		}
		if (correo.length == 0) {
			const mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'No has puesto el Correo!!!';
			message.appendChild(mex);
			return;
		}

		if (!this.compruebaEmail(correo)) {
			const mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'El correo electrónico no tiene un formato valido!!!';
			message.appendChild(mex);
			return;
		}

		if ((await this.loginService.compruebaCorreo(correo)) == false) {
			this.nuevoUsuario.correo_electronico = correo;
			this.nuevoUsuario.nombre_usuario = (document.getElementById('nombre2') as HTMLInputElement).value;
			const content: HTMLDivElement = document.getElementById('content2') as HTMLDivElement;
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
			const mex: HTMLParagraphElement = document.createElement('p');
			mex.textContent = 'Este correo ya está registrado!!!';
			message.appendChild(mex);
			return;
		}
	}

	compruebaPassword() {
		const message: HTMLDivElement = document.getElementById('message4') as HTMLDivElement;
		message.innerHTML = '';
		const pass: string = (document.getElementById('password') as HTMLInputElement).value;
		const pass2: string = (document.getElementById('password2') as HTMLInputElement).value;
		if (pass.length === 0) {
			message.textContent = 'La contraseña no puede estas vacía';
			return;
		}
		if (pass2.length === 0) {
			message.textContent = 'La contraseña no puede estas vacía';
			return;
		}
		if (pass == pass2) {
			this.nuevoUsuario.password = pass;
			this.nuevoUsuario.fecha_registro_usuario = new Date();
			this.registerService.registrarNuevoUsuario(this.nuevoUsuario);
			message.classList.remove('text-red-600');
			message.classList.add('text-green-700');
			message.innerHTML = 'Te has registrado con éxito!!!';
			setTimeout(() => {
				this.close();
			}, 1000);
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
