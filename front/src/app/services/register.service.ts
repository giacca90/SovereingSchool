/* eslint-disable no-async-promise-executor */
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { NuevoUsuario } from '../models/NuevoUsuario';

@Injectable({
	providedIn: 'root',
})
export class RegisterService {
	private apiUrl = 'http://localhost:8080/';
	constructor(private http: HttpClient) {}

	async registrarNuevoUsuario(nuevoUsuario: NuevoUsuario) {
		try {
			await firstValueFrom(this.http.post<string>(`${this.apiUrl}usuario/nuevo`, nuevoUsuario, { responseType: 'text' as 'json' }));
			return true;
		} catch (error) {
			console.error('HTTP request failed:', error);
			return false;
		}
	}
}
