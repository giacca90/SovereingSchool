/* eslint-disable no-async-promise-executor */
import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NuevoUsuario } from '../models/NuevoUsuario';

@Injectable({
	providedIn: 'root',
})
export class RegisterService {
	private apiUrl = 'http://localhost:8080/';
	constructor(private http: HttpClient) {}

	async registrarNuevoUsuario(nuevoUsuario: NuevoUsuario): Promise<boolean> {
		return new Promise(async (resolve, reject) => {
			const sub = this.http.post<string>(`${this.apiUrl}usuario/nuevo`, nuevoUsuario, { observe: 'response', responseType: 'text' as 'json' }).subscribe({
				next: (response: HttpResponse<string>) => {
					if (response.status === 200) {
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
