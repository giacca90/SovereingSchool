import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Curso } from '../models/Curso';

@Injectable({
	providedIn: 'root',
})
export class CursosService {
	backURL: String = 'http://localhost:8080';
	public cursos: Curso[] = [];

	constructor(private http: HttpClient) {
		this.cargaCursos();
	}

	getCurso(id_curso: Number) {
		for (let curso of this.cursos) {
			if (curso.id_curso == id_curso) return curso;
		}
		return null;
	}

	private async cargaCursos() {
		this.http.get<Curso[]>(this.backURL + '/cursos/getAll').subscribe({
			next: (response) => {
				response.map((curso: Curso) => {
					this.cursos.push(curso);
				});
			},
			error: (error: HttpErrorResponse) => {
				console.error('HTTP request failed:', error);
			},
		});
	}
}
