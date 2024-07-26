import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class UsuariosService {
	private apiUrl = 'http://localhost:8080/usuario/';
	public profes: Usuario[] = [];
	constructor(private http: HttpClient) {}

	async getUsuario(id_usuario: number) {
		try {
			const response: Usuario = await firstValueFrom(this.http.get<Usuario>(this.apiUrl + id_usuario));
			return response;
		} catch (error) {
			console.error(error);
			return null;
		}
	}

	getNombreProfe(id: number) {
		return this.profes.find((profe: Usuario) => profe.id_usuario === id)?.nombre_usuario.toString();
	}

	async save(formData: FormData) {
		try {
			const response: string[] = await firstValueFrom(this.http.post<string[]>(this.apiUrl + 'subeFotos', formData));
			return response;
		} catch (error) {
			console.error('Error en save: ' + error);
			return null;
		}
	}

	async actializaUsuario(temp: Usuario) {
		const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
		try {
			await firstValueFrom(this.http.put(this.apiUrl + 'edit', temp, { headers, responseType: 'text' }));
		} catch (error) {
			console.error('Error en actualizar el usuario: ' + error);
		}
	}
}
