import { afterNextRender, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Curso } from '../../models/Curso';
import { CursosService } from '../../services/cursos.service';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-curso',
	standalone: true,
	imports: [],
	templateUrl: './curso.component.html',
	styleUrl: './curso.component.css',
})
export class CursoComponent {
	private id_curso: Number = 0;
	public curso: Curso | null;
	public nombresProfesores: string = '';

	constructor(
		private route: ActivatedRoute,
		private cursoService: CursosService,
		public loginService: LoginService,
	) {
		this.route.params.subscribe((params) => {
			this.id_curso = params['id_curso'];
		});

		this.curso = this.cursoService.getCurso(this.id_curso);
		if (this.curso) {
			if (this.curso.profesores_curso.length == 1) this.nombresProfesores = this.curso.profesores_curso[0].nombre_usuario.toString();
			else {
				let nombres: string = this.curso.profesores_curso[0].nombre_usuario.toString();
				for (let i = 1; i < this.curso.profesores_curso.length; i++) {
					nombres = nombres + ' y ' + this.curso.profesores_curso[i].nombre_usuario;
				}
				this.nombresProfesores = nombres;
			}

			afterNextRender(() => {
				let divClases: HTMLDivElement = document.getElementById('clases') as HTMLDivElement;
				if (this.curso) {
					for (let clase of this.curso.clases_curso) {
						let divInt: HTMLDivElement = document.createElement('div') as HTMLDivElement;
						divInt.classList.add('flex-grow', 'p-2', 'items-start', 'border', 'border-black', 'rounded-lg');
						let p: HTMLParagraphElement = document.createElement('p');
						p.innerHTML = `${clase.posicion_clase}: ${clase.nombre_clase}`;
						divInt.appendChild(p);
						let desc: HTMLParagraphElement = document.createElement('p');
						desc.textContent = clase.descriccion_clase.toString();
						divInt.appendChild(desc);
						divClases.appendChild(divInt);
						divClases.appendChild(document.createElement('br'));
					}

					if (this.curso.planes_curso.length > 0) {
						let p: HTMLParagraphElement = document.createElement('p');
						p.innerHTML = 'Este curso es parte de los siguiente planes:';
						divClases.appendChild(p);
						for (let plan of this.curso.planes_curso) {
							let p: HTMLParagraphElement = document.createElement('p');
							p.innerHTML = plan.nombre_plan.toString();
							divClases.appendChild(p);
						}
					}

					let boton: HTMLButtonElement = document.getElementById('boton') as HTMLButtonElement;
					if (this.loginService.usuario?.cursos_usuario)
						for (let cursoUsuario of this.loginService.usuario?.cursos_usuario) {
							if (cursoUsuario.id_curso == this.curso.id_curso) {
								boton.textContent = 'Ir al curso';
								break;
							}
						}
					for (let planes of this.curso.planes_curso) {
						if (planes.id_plan == this.loginService.usuario?.plan_usuario) {
							boton.textContent = 'Ir al curso';
							let nota: HTMLParagraphElement = document.getElementById('nota') as HTMLParagraphElement;
							nota.innerHTML = 'Este curso es parte de tu Plan ' + planes.nombre_plan;
						}
					}
				}
			});
		}
	}
}
