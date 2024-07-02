/* eslint-disable no-async-promise-executor */
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NuevoUsuario } from '../models/NuevoUsuario';

@Injectable({
	providedIn: 'root',
})
export class RegisterService {
	private apiUrl = 'http://localhost:8080/';
	constructor(private http: HttpClient) {}

	async registrarNuevoUsuario(nuevoUsuario: NuevoUsuario) {
		return new Promise(async (resolve, reject) => {
			this.http.post<string>(`${this.apiUrl}usuario/nuevo`, nuevoUsuario, { responseType: 'text' as 'json' }).subscribe({
				next: (response) => {
					console.log('HTTP response received:', response);
					resolve(true);
				},
				error: (error: HttpErrorResponse) => {
					console.error('HTTP request failed:', error);
					reject(false);
				},
			});
		});
	}
}
