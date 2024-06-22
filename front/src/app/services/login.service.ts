import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { afterRender, Injectable } from '@angular/core';
import { Usuario } from '../models/usuario';

@Injectable({
	providedIn: 'root',
})
export class LoginService {
	private apiUrl = 'http://localhost:8080/login/';
	private id_usuario: Number | null = null;
	public usuario: Usuario | null = null;

	constructor(private http: HttpClient) {
		afterRender(() => {
			let usuario_guardado: string | null = localStorage.getItem('Usuario');
			if (usuario_guardado) this.usuario = JSON.parse(usuario_guardado);
		});
	}

	async compruebaCorreo(correo: string): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			this.http.get<number>(`${this.apiUrl}${correo}`).subscribe({
				next: (response) => {
					console.log('HTTP response received:', response);
					if (response == 0) {
						resolve(false);
					}

					if (response > 0) {
						this.id_usuario = response;
						resolve(true);
					}
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					reject(false);
				},
			});
		});
	}

	async compruebaPassword(password: String): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			this.http.get<Usuario>(this.apiUrl + this.id_usuario + '/' + password).subscribe({
				next: (response) => {
					if (response.getId === null) {
						resolve(false);
						return;
					}
					this.usuario = response;
					afterRender(() => {
						localStorage.setItem('Usuario', JSON.stringify(this.usuario));
					});
					console.log('LOG: ' + JSON.stringify(this.usuario));
					resolve(true);
					return;
				},
				error: (error: HttpErrorResponse) => {
					//console.error('HTTP request failed:', error);
					resolve(false);
				},
			});
		});
	}
}
