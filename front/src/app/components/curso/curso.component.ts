/* eslint-disable no-unsafe-optional-chaining */
import { afterNextRender, Component } from '@angular/core';
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
	public curso: Curso | null;
	public nombresProfesores: string | undefined = '';

	constructor(
		private route: ActivatedRoute,
		private cursoService: CursosService,
		private usuarioService: UsuariosService,
		public loginService: LoginService,
	) {
		this.route.params.subscribe((params) => {
			this.id_curso = params['id_curso'];
		});

		this.curso = this.cursoService.getCurso(this.id_curso);
		if (this.curso) {
			if (this.curso.profesores_curso.length == 1) this.nombresProfesores = this.usuarioService.getNombreProfe(this.curso.profesores_curso[0]);
			else {
				let nombres: string | undefined = this.usuarioService.getNombreProfe(this.curso.profesores_curso[0])?.toString();
				for (let i = 1; i < this.curso.profesores_curso.length; i++) {
					nombres = nombres + ' y ' + this.usuarioService.getNombreProfe(this.curso.profesores_curso[i]);
				}
				this.nombresProfesores = nombres;
			}

			afterNextRender(() => {
				const divClases: HTMLDivElement = document.getElementById('clases') as HTMLDivElement;
				if (this.curso) {
					if (this.curso.clases_curso) {
						for (const clase of this.curso.clases_curso) {
							const divInt: HTMLDivElement = document.createElement('div') as HTMLDivElement;
							divInt.classList.add('flex-grow', 'p-2', 'items-start', 'border', 'border-black', 'rounded-lg');
							const p: HTMLParagraphElement = document.createElement('p');
							p.innerHTML = `${clase.posicion_clase}: ${clase.nombre_clase}`;
							divInt.appendChild(p);
							const desc: HTMLParagraphElement = document.createElement('p');
							desc.textContent = clase.descriccion_clase.toString();
							divInt.appendChild(desc);
							divClases.appendChild(divInt);
							divClases.appendChild(document.createElement('br'));
						}
					}

					if (this.curso.planes_curso && this.curso.planes_curso.length > 0) {
						const p: HTMLParagraphElement = document.createElement('p');
						p.innerHTML = 'Este curso es parte de los siguiente planes:';
						divClases.appendChild(p);
						for (const plan of this.curso.planes_curso) {
							const p: HTMLParagraphElement = document.createElement('p');
							p.innerHTML = plan.nombre_plan.toString();
							divClases.appendChild(p);
						}
					}
					if (this.loginService.usuario) {
						const boton: HTMLButtonElement = document.getElementById('boton') as HTMLButtonElement;
						if (this.loginService.usuario?.cursos_usuario)
							for (const cursoUsuario of this.loginService.usuario?.cursos_usuario) {
								if (cursoUsuario.id_curso == this.curso.id_curso) {
									boton.textContent = 'Ir al curso';
									break;
								}
							}
						if (this.curso.planes_curso) {
							for (const planes of this.curso.planes_curso) {
								if (planes.id_plan == this.loginService.usuario?.plan_usuario) {
									boton.textContent = 'Ir al curso';
									const nota: HTMLParagraphElement = document.getElementById('nota') as HTMLParagraphElement;
									nota.innerHTML = 'Este curso es parte de tu Plan ' + planes.nombre_plan;
								}
							}
						}
					}
				}
			});
		}
	}
}
