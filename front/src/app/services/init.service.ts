import { HttpClient } from '@angular/common/http';
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
		this.http.get<Init>(this.apiUrl).subscribe({
			next: (response: Init) => {
				response.profesInit.forEach((profe) => {
					this.usuarioService.profes.push(new Usuario(profe.id_usuario, profe.nombre_usuario, profe.foto_usuario, profe.presentacion));
				});
				response.cursosInit.forEach((curso) => {
					const profes: Usuario[] = [];
					curso.profesores_curso.forEach((id) => {
						const prof = response.profesInit.find((profe) => profe.id_usuario === id);
						if (prof) {
							profes.push(prof);
						}
					});
					this.cursoService.cursos.push(new Curso(curso.id_curso, curso.nombre_curso, profes, curso.descriccion_corta, curso.imagen_curso));
				});
				this.estadistica = response.estadistica;
			},
			error(e: Error) {
				console.error('Error en init: ' + e.message);
			},
		});
	}
}
