import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Curso } from '../models/Curso';
import { Estadistica } from '../models/Estadistica';
import { Init } from '../models/Init';
import { Usuario } from '../models/Usuario';
import { CursosService } from './cursos.service';
import { UsuariosService } from './usuarios.service';

@Injectable({
	providedIn: 'root',
})
export class InitService {
	private apiUrl = 'http://localhost:8080/init';
	public estadistica: Estadistica | null = null;
	constructor(
		private http: HttpClient,
		private cursoService: CursosService,
		private usuarioService: UsuariosService,
	) {
		this.carga();
	}

	carga() {
		this.usuarioService.profes = [];
		this.cursoService.cursos = [];
		const sub = this.http.get<Init>(this.apiUrl, { observe: 'response' }).subscribe({
			next: (response: HttpResponse<Init>) => {
				if (response.ok && response.body) {
					response.body.profesInit.forEach((profe) => {
						this.usuarioService.profes.push(new Usuario(profe.id_usuario, profe.nombre_usuario, profe.foto_usuario, profe.presentacion));
					});
					response.body.cursosInit.forEach((curso) => {
						const profes: Usuario[] = [];
						curso.profesores_curso.forEach((id) => {
							const prof = response.body?.profesInit.find((profe) => profe.id_usuario === id);
							if (prof) {
								profes.push(prof);
							}
						});
						this.cursoService.cursos.push(new Curso(curso.id_curso, curso.nombre_curso, profes, curso.descriccion_corta, curso.imagen_curso));
					});
					this.estadistica = response.body.estadistica;
				}
			},
			error(e: Error) {
				console.error('Error en init: ' + e.message);
				sub.unsubscribe();
			},
		});
	}
}
