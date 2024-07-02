import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class UsuariosService {
	private apiUrl = 'http://localhost:8080/usuario/';
	constructor(private http: HttpClient) {}

	getUsuario(id_usuario: Number): any {
		console.log('Consulta: ' + this.apiUrl + id_usuario);
		this.http.get<Usuario>(this.apiUrl + id_usuario).subscribe({
			next: (response: Usuario) => {
				return response;
			},
			error: (e: Error) => {
				return null;
			},
		});
	}
}
