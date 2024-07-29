import { HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, firstValueFrom, map, Observable, of, throwError } from 'rxjs';
import { Clase } from '../models/Clase';
import { Curso } from '../models/Curso';

@Injectable({
	providedIn: 'root',
})
export class CursosService {
	backURL: string = 'http://localhost:8080';
	public cursos: Curso[] = [];

	constructor(private http: HttpClient) {}

	async getCurso(id_curso: number) {
		for (let i = 0; i < this.cursos.length; i++) {
			if (this.cursos[i].id_curso == id_curso) {
				if (this.cursos[i].clases_curso === undefined) {
					try {
						const response = await firstValueFrom(this.http.get<Curso>(`${this.backURL}/cursos/getCurso/${id_curso}`));
						this.cursos[i].clases_curso = response.clases_curso?.sort((a, b) => a.posicion_clase - b.posicion_clase);
						this.cursos[i].descriccion_larga = response.descriccion_larga;
						this.cursos[i].fecha_publicacion_curso = response.fecha_publicacion_curso;
						this.cursos[i].planes_curso = response.planes_curso;
						this.cursos[i].precio_curso = response.precio_curso;
						return this.cursos[i];
					} catch (error) {
						console.error('Error en cargar curso:', error);
						return null;
					}
				} else return this.cursos[i];
			}
		}
		return null;
	}

	updateCurso(curso: Curso | null): Observable<boolean> {
		if (curso) {
			const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
			return this.http.put<string>(`${this.backURL}/cursos/update`, curso, { headers, observe: 'response' }).pipe(
				map((response: HttpResponse<string>) => {
					if (response.status === 200) {
						const index = this.cursos.findIndex((cur) => cur.id_curso === curso.id_curso);
						if (index !== -1) {
							this.cursos[index] = JSON.parse(JSON.stringify(curso));
						}
						return true;
					} else {
						return false;
					}
				}),
				catchError((error: HttpErrorResponse) => {
					console.error('Error al actualizar: ' + error.message);
					return throwError(() => new Error('Error al actualizar'));
				}),
			);
		} else {
			return throwError(() => new Error('El curso es nulo'));
		}
	}

	editClass(editar: Clase): Observable<boolean> {
		const curso_clase: number | undefined = editar.curso_clase;
		editar.curso_clase = undefined;
		return this.http.put<string>(`${this.backURL}/cursos/${curso_clase}/editClase`, editar, { responseType: 'text' as 'json' }).pipe(
			map(() => {
				return true;
			}),
			catchError((error: Error) => {
				console.error('Error en editar la clase: ' + error.message);
				return of(false);
			}),
		);
	}

	createClass(editar: Clase): Observable<boolean> {
		const curso_clase: number | undefined = editar.curso_clase;
		editar.curso_clase = undefined;
		return this.http.put<string>(this.backURL + '/cursos/' + curso_clase + '/addClase', editar, { responseType: 'text' as 'json' }).pipe(
			map(() => {
				this.cursos[this.cursos.findIndex((curso) => curso.id_curso === curso_clase)].clases_curso = undefined;
				return true;
			}),
			catchError((e: Error) => {
				console.error('Error en crear la clase: ' + e.message);
				return of(false);
			}),
		);
	}

	deleteClass(clase: Clase): Observable<boolean> {
		const curso_clase: number | undefined = clase.curso_clase;
		clase.curso_clase = undefined;
		return this.http.delete<string>(this.backURL + '/cursos/' + curso_clase + '/deleteClase/' + clase.id_clase, { responseType: 'text' as 'json' }).pipe(
			map(() => {
				this.cursos[this.cursos.findIndex((curso) => curso.id_curso === curso_clase)].clases_curso = undefined;
				return true;
			}),
			catchError((e: Error) => {
				console.error('Error en crear la clase: ' + e.message);
				return of(false);
			}),
		);
	}

	subeVideo(file: File): Observable<string | null> {
		const formData = new FormData();
		formData.append('video', file, file.name);

		return this.http.post<string>(this.backURL + '/cursos/subeVideo', formData, { responseType: 'text' as 'json' }).pipe(
			map((response) => {
				return response;
			}),
			catchError((e: Error) => {
				console.error('Errorn en subur el video: ' + e.message);
				return of(null);
			}),
		);
	}
}
