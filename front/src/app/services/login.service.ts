import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { afterNextRender, Injectable } from '@angular/core';
import { Auth } from '../models/Auth';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class LoginService {
	private apiUrl = 'https://localhost:8080/login/';
	private id_usuario: number | null = null;
	public usuario: Usuario | null = null;

	constructor(private http: HttpClient) {
		afterNextRender(() => {
			const usuario_guardado: string | null = localStorage.getItem('Usuario');
			if (usuario_guardado) {
				this.usuario = JSON.parse(usuario_guardado);
			}
		});
	}

	async compruebaCorreo(correo: string): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			const sub = this.http.get<number>(`${this.apiUrl}${correo}`, { observe: 'response' }).subscribe({
				next: (response: HttpResponse<number>) => {
					if (response.ok) {
						if (response.body == 0) {
							resolve(false);
							sub.unsubscribe();
						} else {
							this.id_usuario = response.body;
							resolve(true);
							sub.unsubscribe();
						}
					} else {
						console.error('Error en comprobar el correo: ' + response.status);
						reject(false);
					}
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					reject(false);
					sub.unsubscribe();
				},
			});
		});
	}

	async compruebaPassword(password: string): Promise<boolean> {
		return new Promise(async (resolve) => {
			const sub = this.http.get<Auth>(this.apiUrl + this.id_usuario + '/' + password, { observe: 'response' }).subscribe({
				next: (response: HttpResponse<Auth>) => {
					if (response.ok && response.body) {
						if (!response.body.status && response.body.usuario === null) {
							resolve(false);
							sub.unsubscribe();
							return;
						}
						this.usuario = response.body.usuario;
						// Comprueba si está en el navegador
						localStorage.setItem('Usuario', JSON.stringify(this.usuario));
						localStorage.setItem('Token', response.body.accessToken);
						resolve(true);
						sub.unsubscribe();
						return;
					} else {
						console.error('Error en comprobar las password: ' + response.status);
						return;
					}
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					resolve(false);
					sub.unsubscribe();
				},
			});
		});
	}
}
