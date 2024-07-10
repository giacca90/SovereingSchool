/* eslint-disable no-unsafe-optional-chaining */
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Curso } from '../../models/Curso';
import { CursosService } from '../../services/cursos.service';
import { LoginService } from '../../services/login.service';
import { UsuariosService } from '../../services/usuarios.service';

@Component({
	selector: 'app-curso',
	standalone: true,
	imports: [],
	templateUrl: './curso.component.html',
	styleUrl: './curso.component.css',
})
export class CursoComponent {
	private id_curso: number = 0;
	public curso: Curso | null = null;
	public nombresProfesores: string | undefined = '';

	constructor(
		private route: ActivatedRoute,
		private cursoService: CursosService,
		private usuarioService: UsuariosService,
		private cdr: ChangeDetectorRef,
		public loginService: LoginService,
	) {
		this.route.params.subscribe((params) => {
			this.id_curso = params['id_curso'];
		});

		this.cursoService.getCurso(this.id_curso).then((curso) => {
			this.curso = curso;
			if (this.curso) {
				console.log('Recibido el curso: ' + this.curso.descriccion_larga);
				if (this.curso.profesores_curso.length == 1) this.nombresProfesores = this.usuarioService.getNombreProfe(this.curso.profesores_curso[0]);
				else {
					let nombres: string | undefined = this.usuarioService.getNombreProfe(this.curso.profesores_curso[0])?.toString();
					for (let i = 1; i < this.curso.profesores_curso.length; i++) {
						nombres = nombres + ' y ' + this.usuarioService.getNombreProfe(this.curso.profesores_curso[i]);
					}
					this.nombresProfesores = nombres;
				}
			}
		});
	}
}
