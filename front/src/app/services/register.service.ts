import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Auth } from '../models/Auth';
import { NuevoUsuario } from '../models/NuevoUsuario';

@Injectable({
	providedIn: 'root',
})
export class RegisterService {
	private apiUrl = 'https://localhost:8080/';
	constructor(private http: HttpClient) {}

	async registrarNuevoUsuario(nuevoUsuario: NuevoUsuario): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			const sub = this.http.post<Auth>(`${this.apiUrl}usuario/nuevo`, nuevoUsuario, { observe: 'response', responseType: 'text' as 'json' }).subscribe({
				next: (response: HttpResponse<Auth>) => {
					if (response.status === 200 && response.body) {
						localStorage.setItem('Usuario', JSON.stringify(response.body.usuario));
						localStorage.setItem('Token', response.body.accessToken);
						resolve(true);
						sub.unsubscribe();
					} else {
						resolve(false);
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
}
