import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, firstValueFrom, map, Observable, of } from 'rxjs';
import { Clase } from '../models/Clase';
import { Curso } from '../models/Curso';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class CursosService {
	backURL: string = 'https://localhost:8080';
	backURLStreaming: string = 'https://localhost:8090';
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
		if (curso === null) {
			console.error('El curso no existe!!!');
			return of(false);
		}
		return this.http.put<string>(`${this.backURL}/cursos/update`, curso, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response) => {
				if (response.ok) {
					return true;
				} else {
					console.error('Respuesta del back: ' + response.body);
					return false;
				}
			}),
			catchError((e: Error) => {
				console.error('Error en actualizar el curso: ' + e.message);
				return of(false);
			}),
		);
	}

	/* updateCurso(curso: Curso | null): Observable<boolean> {
		if (curso) {
			if (curso.id_curso === 0 && curso.clases_curso) {
				const clases: Clase[] = curso.clases_curso;
				curso.clases_curso = [];

				return this.http.post<number>(`${this.backURL}/cursos/new`, curso, { observe: 'response' }).pipe(
					switchMap((response: HttpResponse<number>) => {
						if (response.ok && response.body) {
							curso.id_curso = response.body;
							return from(clases).pipe(
								concatMap((clase) => {
									clase.curso_clase = curso.id_curso;
									return this.guardarCambiosClase(clase);
								}),
								reduce((acc, value) => acc && value, true), // Acumula un booleano para indicar si todas las clases se crearon correctamente
							);
						} else {
							return throwError(() => new Error('Error al crear el curso'));
						}
					}),
					catchError((error) => {
						console.error('Error al actualizar el curso:', error);
						return throwError(() => new Error('Error al actualizar'));
					}),
				);
			} else {
				return this.http.put<string>(`${this.backURL}/cursos/update`, curso, { observe: 'response', responseType: 'text' as 'json' }).pipe(
					map((response: HttpResponse<string>) => {
						if (response.ok) {
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
			}
		} else {
			return throwError(() => new Error('El curso es nulo'));
		}
	} */

	/* 	editClass(editar: Clase): Observable<boolean> {
		const curso_clase: number | undefined = editar.curso_clase;
		editar.curso_clase = undefined;
		return this.http.put<string>(`${this.backURL}/cursos/${curso_clase}/editClase`, editar, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.ok) {
					return true;
				}
				return false;
			}),
			catchError((error: Error) => {
				console.error('Error en editar la clase: ' + error.message);
				return of(false);
			}),
		);
	} */

	/* 	createClass(editar: Clase): Observable<boolean> {
		const curso_clase: number | undefined = editar.curso_clase;
		editar.curso_clase = undefined;
		return this.http.put<string>(this.backURL + '/cursos/' + curso_clase + '/addClase', editar, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.ok) {
					this.cursos[this.cursos.findIndex((curso) => curso.id_curso === curso_clase)].clases_curso = [];
					return true;
				}
				return false;
			}),
			catchError((e: Error) => {
				console.error('Error en crear la clase: ' + e.message);
				return of(false);
			}),
		);
	} */

	deleteClass(clase: Clase): Observable<boolean> {
		const curso_clase: number | undefined = clase.curso_clase;
		clase.curso_clase = undefined;
		return this.http.delete<string>(this.backURL + '/cursos/' + curso_clase + '/deleteClase/' + clase.id_clase, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.ok) {
					this.cursos[this.cursos.findIndex((curso) => curso.id_curso === curso_clase)].clases_curso = undefined;
					return true;
				}
				return false;
			}),
			catchError((e: Error) => {
				console.error('Error en crear la clase: ' + e.message);
				return of(false);
			}),
		);
	}

	subeVideo(file: File, idCurso: number, idClase: number): Observable<string | null> {
		const formData = new FormData();
		formData.append('video', file, file.name);

		return this.http.post<string>(this.backURL + '/cursos/subeVideo/' + idCurso + '/' + idClase, formData, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.ok) {
					return response.body;
				}
				return null;
			}),
			catchError((e: Error) => {
				console.error('Error en subir el video: ' + e.message);
				return of(null);
			}),
		);
	}

	addImagenCurso(target: FormData): Observable<string | null> {
		return this.http.post<string[]>(this.backURL + '/usuario/subeFotos', target, { observe: 'response' }).pipe(
			map((response: HttpResponse<string[]>) => {
				if (response.ok && response.body) {
					return response.body[0];
				}
				return null;
			}),
			catchError((e: Error) => {
				console.error('Error en subir la imagen: ' + e.message);
				return of(null);
			}),
		);
	}

	getCursosProfe(profe: Usuario) {
		const cursosProfe: Curso[] = [];
		this.cursos.forEach((curso) => {
			curso.profesores_curso.forEach((profe2) => {
				if (profe2.id_usuario === profe.id_usuario) {
					cursosProfe.push(curso);
				}
			});
		});
		return cursosProfe;
	}

	deleteCurso(curso: Curso): Observable<boolean> {
		return this.http.delete<string>(this.backURL + '/cursos/delete/' + curso.id_curso, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.ok) {
					this.cursos = this.cursos.slice(
						this.cursos.findIndex((curso2) => curso2.id_curso === curso.id_curso),
						1,
					);
					return true;
				}
				return false;
			}),
			catchError((e: Error) => {
				console.error('Error en eliminar el curso: ' + e.message);
				return of(false);
			}),
		);
	}

	/* 	guardarCambiosClase(editar: Clase): Observable<boolean> {
		if (editar.id_clase === 0) {
			return this.createClass(editar).pipe(
				map((resp: boolean) => {
					if (resp) {
						this.getCurso(editar.curso_clase).then((response) => {
							console.log(response);
						return true;
					} else {
						console.error('Error en actualizar!!!');
						return false;
					}
				}),
				catchError((e: Error) => {
					console.error('Error en crear la clase: ' + e.message);
					return of(false);
				}),
			);
		} else {
			return this.editClass(editar).pipe(
				map((resp: boolean) => {
					if (resp) {
						return true;
					} else {
						console.error('Error en actualizar!!!');
						return false;
					}
				}),
				catchError((e: Error) => {
					console.error('Error en editar la clase: ' + e.message);
					return of(false);
				}),
			);
		}
	}
 */
	getStatusCurso(id_usuario: number, id_curso: number): Observable<number | boolean> {
		return this.http.get<number>(this.backURLStreaming + '/status/' + id_usuario + '/' + id_curso, { observe: 'response' }).pipe(
			map((response: HttpResponse<number>) => {
				if (response.ok && response.body) {
					return response.body;
				}
				return false;
			}),
		);
	}
}
