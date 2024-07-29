import { HttpClient } from '@angular/common/http';
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
		this.http.get<Usuario>(this.apiUrl + id_usuario).subscribe({
			next: (response: Usuario) => {
				return response;
			},
			error: (e: Error) => {
				console.error(e.message);
				return null;
			},
		});
	}

	getNombreProfe(id: number) {
		return this.profes.find((profe: Usuario) => profe.id_usuario === id)?.nombre_usuario.toString();
	}
}
