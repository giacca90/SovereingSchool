/* eslint-disable no-async-promise-executor */
import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { afterNextRender, Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class LoginService {
	private apiUrl = 'http://localhost:8080/login/';
	private id_usuario: number | null = null;

	// BehaviorSubject para mantener el estado del usuario y emitir los cambios
	private usuarioSubject: BehaviorSubject<Usuario | null> = new BehaviorSubject<Usuario | null>(null);
	public usuario$: Observable<Usuario | null> = this.usuarioSubject.asObservable();

	constructor(private http: HttpClient) {
		afterNextRender(() => {
			const usuario_guardado: string | null = localStorage.getItem('Usuario');
			if (usuario_guardado) {
				// Emitir el usuario guardado en el BehaviorSubject
				this.usuarioSubject.next(JSON.parse(usuario_guardado));
			}
		});
	}

	// Método para comprobar el correo
	async compruebaCorreo(correo: string): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			const sub = this.http.get<number>(`${this.apiUrl}${correo}`, { observe: 'response' }).subscribe({
				next: (response: HttpResponse<number>) => {
					if (response.ok) {
						if (response.body === 0) {
							resolve(false);
						} else {
							this.id_usuario = response.body;
							resolve(true);
						}
					} else {
						console.error('Error en comprobar el correo: ' + response.status);
						reject(false);
					}
					sub.unsubscribe();
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					reject(false);
					sub.unsubscribe();
				},
			});
		});
	}

	// Método para comprobar la contraseña
	async compruebaPassword(password: string): Promise<boolean> {
		return new Promise(async (resolve) => {
			const sub = this.http.get<Usuario>(`${this.apiUrl}${this.id_usuario}/${password}`, { observe: 'response' }).subscribe({
				next: (response: HttpResponse<Usuario>) => {
					if (response.ok && response.body) {
						if (response.body.id_usuario === null) {
							resolve(false);
							sub.unsubscribe();
							return;
						}

						// Emitir el nuevo usuario en el BehaviorSubject
						this.usuarioSubject.next(response.body);

						// Guardar el usuario en localStorage
						localStorage.setItem('Usuario', JSON.stringify(response.body));

						resolve(true);
					} else {
						console.error('Error en comprobar la password: ' + response.status);
						resolve(false);
					}
					sub.unsubscribe();
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					resolve(false);
					sub.unsubscribe();
				},
			});
		});
	}

	// Método para cerrar sesión
	logout() {
		// Limpiar el usuario tanto en el BehaviorSubject como en localStorage
		this.usuarioSubject.next(null);
		localStorage.removeItem('Usuario');
		this.id_usuario = null;
	}

	salir() {
		// Emitir null para indicar que no hay un usuario logueado
		this.usuarioSubject.next(null);

		// Limpiar el usuario de localStorage también
		localStorage.removeItem('Usuario');

		// Restablecer la variable id_usuario a null
		this.id_usuario = null;
	}
}
