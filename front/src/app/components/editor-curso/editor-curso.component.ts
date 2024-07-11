import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Curso } from '../../models/Curso';
import { CursosService } from '../../services/cursos.service';

@Component({
	selector: 'app-editor-curso',
	standalone: true,
	imports: [],
	templateUrl: './editor-curso.component.html',
	styleUrl: './editor-curso.component.css',
})
export class EditorCursoComponent {
	id_curso: number = 0;
	curso: Curso | null = null;
	constructor(
		public cursoService: CursosService,
		private route: ActivatedRoute,
	) {
		this.route.params.subscribe((params) => {
			this.id_curso = params['id_curso'];
			this.cursoService.getCurso(this.id_curso).then((curso) => (this.curso = curso));
		});
	}
}
