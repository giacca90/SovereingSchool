/* eslint-disable no-async-promise-executor */
import { HttpClient } from '@angular/common/http';
import { afterNextRender, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
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

	async compruebaCorreo(correo: string) {
		try {
			const response: number = await firstValueFrom(this.http.get<number>(`${this.apiUrl}${correo}`));
			if (response == 0) {
				return false;
			} else {
				this.id_usuario = response;
				return true;
			}
		} catch (error) {
			console.error('HTTP request failed:', error);
			return false;
		}
	}

	async compruebaPassword(password: string) {
		try {
			const response: Usuario = await firstValueFrom(this.http.get<Usuario>(this.apiUrl + this.id_usuario + '/' + password));
			if (response.id_usuario === null) {
				return false;
			}
			this.usuario = response;
			localStorage.setItem('Usuario', JSON.stringify(this.usuario));
			return true;
		} catch (error) {
			console.error('HTTP request failed:', error);
			return false;
		}
	}
}
