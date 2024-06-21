import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Usuario } from '../models/usuario';

@Injectable({
	providedIn: 'root',
})
export class LoginService {
	private apiUrl = 'http://localhost:8080/login/'; // URL de la API
	private id_usuario: Number | null = null;
	public usuario: Usuario | null = null;

	constructor(private http: HttpClient) {}

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
					//console.log('HTTP response received:', response);
					if (response.getId === null) resolve(false);
					resolve(true);
				},
				error: (error: HttpErrorResponse) => {
					//console.error('HTTP request failed:', error);
					resolve(false);
				},
			});
		});
	}
}
