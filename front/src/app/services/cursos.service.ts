import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
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

	async updateCurso(curso: Curso | null) {
		if (curso) {
			const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
			try {
				const response: HttpResponse<string> = await firstValueFrom(this.http.put<string>(`${this.backURL}/cursos/update`, curso, { headers, observe: 'response' }));
				if (response.status === 200) {
					const index = this.cursos.findIndex((cur) => cur.id_curso === curso.id_curso);
					if (index !== -1) {
						this.cursos[index] = JSON.parse(JSON.stringify(curso));
					}
					return true;
				} else {
					return false;
				}
			} catch (error) {
				console.error('Error en actualizar curso: ' + error);
				return false;
			}
		}
		return false;
	}

	async editClass(editar: Clase) {
		const curso_clase: number | undefined = editar.curso_clase;
		editar.curso_clase = undefined;
		try {
			await firstValueFrom(this.http.put(`${this.backURL}/cursos/${curso_clase}/editClase`, editar, { responseType: 'text' as 'json' }));
			return true;
		} catch (error) {
			console.error('Error en editar la clase: ' + error);
			return false;
		}
	}

	async createClass(editar: Clase) {
		const curso_clase: number | undefined = editar.curso_clase;
		editar.curso_clase = undefined;
		try {
			await firstValueFrom(this.http.put<string>(this.backURL + '/cursos/' + curso_clase + '/addClase', editar, { responseType: 'text' as 'json' }));
			this.cursos[this.cursos.findIndex((curso) => curso.id_curso === curso_clase)].clases_curso = undefined;
			return true;
		} catch (error) {
			console.error('Error en crear la clase: ' + error);
			return false;
		}
	}

	async deleteClass(clase: Clase) {
		const curso_clase: number | undefined = clase.curso_clase;
		clase.curso_clase = undefined;
		try {
			await firstValueFrom(this.http.delete<string>(this.backURL + '/cursos/' + curso_clase + '/deleteClase/' + clase.id_clase, { responseType: 'text' as 'json' }));
			this.cursos[this.cursos.findIndex((curso) => curso.id_curso === curso_clase)].clases_curso = undefined;
			return true;
		} catch (error) {
			console.error('Error en crear la clase: ' + error);
			return false;
		}
	}
}
