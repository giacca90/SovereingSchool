/* eslint-disable no-async-promise-executor */
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, Injectable } from '@angular/core';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class LoginService {
	private apiUrl = 'http://localhost:8080/login/';
	private id_usuario: number | null = null;
	public usuario: Usuario | null = null;

	constructor(private http: HttpClient) {
		afterNextRender(() => {
			const usuario_guardado: string | null = localStorage.getItem('Usuario');
			if (usuario_guardado) this.usuario = JSON.parse(usuario_guardado);
		});
	}

	async compruebaCorreo(correo: string): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			this.http.get<number>(`${this.apiUrl}${correo}`).subscribe({
				next: (response) => {
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

	async compruebaPassword(password: string): Promise<boolean> {
		return new Promise(async (resolve) => {
			this.http.get<Usuario>(this.apiUrl + this.id_usuario + '/' + password).subscribe({
				next: (response) => {
					if (response.id_usuario === null) {
						resolve(false);
						return;
					}
					this.usuario = response;
					localStorage.setItem('Usuario', JSON.stringify(this.usuario));
					resolve(true);
					return;
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					resolve(false);
				},
			});
		});
	}
}
