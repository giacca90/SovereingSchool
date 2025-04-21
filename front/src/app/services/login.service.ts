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
			const sub = this.http.get<Auth>(this.apiUrl + this.id_usuario + '/' + password, { observe: 'response', withCredentials: true }).subscribe({
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

	async refreshToken(): Promise<string | null> {
		return new Promise(async (resolve, reject) => {
			const sub = this.http.get<Auth>(this.apiUrl + 'refresh/' + this.id_usuario, { observe: 'response', withCredentials: true }).subscribe({
				next: (response: HttpResponse<Auth>) => {
					if (response.ok && response.body) {
						localStorage.setItem('Token', response.body.accessToken);
						resolve(response.body.accessToken);
						sub.unsubscribe();
					} else {
						console.error('Error en refrescar el token: ' + response.status);
						reject(null);
					}
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					reject(null);
					sub.unsubscribe();
				},
			});
		});
	}

	logout(): void {
		this.usuario = null;
		this.id_usuario = null;
		localStorage.clear();
		this.http.get<string>(this.apiUrl + 'logout', { observe: 'response', withCredentials: true }).subscribe({
			next: (response: HttpResponse<string>) => {
				if (response.status !== 200) {
					console.error('Error en logout: ' + response.status);
					console.error(response.body);
				}
			},
			error: (error: HttpErrorResponse) => {
				console.error('Logout failed:', error);
			},
		});
	}
}
