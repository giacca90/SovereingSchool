import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Curso } from '../models/Curso';

@Injectable({
	providedIn: 'root',
})
export class CursosService {
	backURL: string = 'http://localhost:8080';
	public cursos: Curso[] = [];

	constructor(private http: HttpClient) {}

	getCurso(id_curso: number) {
		for (const curso of this.cursos) {
			if (curso.id_curso == id_curso) {
				if (!curso.clases_curso) {
					this.http.get<Curso>(this.backURL + '/cursos/getCurso/' + id_curso).subscribe({
						next: (response) => {
							this.cursos[this.cursos.findIndex((curso) => curso.id_curso === id_curso)] = response;
						},
						error(e: Error) {
							console.error('Error en cargar curso: ' + e.message);
						},
					});
				}
				return curso;
			}
		}
		return null;
	}

	/* private async cargaCursos() {
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
	} */
}
