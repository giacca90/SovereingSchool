import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class UsuariosService {
	private apiUrl = 'http://localhost:8080/usuario/';
	public profes: Usuario[] = [];
	constructor(private http: HttpClient) {
		this.http.get<Usuario[]>(this.apiUrl + 'profes').subscribe({
			next: (response: Usuario[]) => {
				this.profes = response;
				console.log(response);
			},
			error: (e: Error) => {
				console.log('Error al cargar profes: ' + e.message);
			},
		});
	}

	getUsuario(id_usuario: number) {
		console.log('Consulta: ' + this.apiUrl + id_usuario);
		this.http.get<Usuario>(this.apiUrl + id_usuario).subscribe({
			next: (response: Usuario) => {
				return response;
			},
			error: (e: Error) => {
				console.error(e.message)
				return null;
			},
		});
	}
}
