import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class UsuariosService {
	private apiUrl = 'http://localhost:8080/usuario/';
	public profes: Usuario[] = [];
	constructor(private http: HttpClient) {}

	getUsuario(id_usuario: number) {
		const sub = this.http.get<Usuario>(this.apiUrl + id_usuario).subscribe({
			next: (response: Usuario) => {
				sub.unsubscribe();
				return response;
			},
			error: (e: Error) => {
				sub.unsubscribe();
				console.error(e.message);
				return null;
			},
		});
	}

	getNombreProfe(id: number) {
		return this.profes.find((profe: Usuario) => profe.id_usuario === id)?.nombre_usuario.toString();
	}

	save(formData: FormData) {
		return this.http.post<string[]>(this.apiUrl + 'subeFotos', formData);
	}

	actualizaUsuario(temp: Usuario) {
		const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
		return this.http.put(this.apiUrl + 'edit', temp, { headers, responseType: 'text' });
	}
}
