/* eslint-disable no-unsafe-optional-chaining */
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Curso } from '../../models/Curso';
import { Plan } from '../../models/Plan';
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
		public router: Router,
	) {
		this.route.params.subscribe((params) => {
			this.id_curso = params['id_curso'];
		});

		this.cursoService.getCurso(this.id_curso).then((curso) => {
			this.curso = curso;
			if (this.curso) {
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

	compruebaPlan(planUsuario: Plan | undefined): Plan | null {
		if (planUsuario !== undefined && this.curso?.planes_curso) {
			for (const plan of this.curso.planes_curso) {
				if (plan.id_plan == planUsuario.id_plan) {
					return plan;
				}
			}
		}
		return null;
	}
}
