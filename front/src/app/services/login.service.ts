import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class LoginService {
	private apiUrl = 'http://localhost:8080/login/'; // URL de la API
	private id_usuario: Number | null = null;

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

	compruebaPassword(password: String): Boolean {
		return false;
	}
}
