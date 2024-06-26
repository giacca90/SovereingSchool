import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Curso } from '../../models/Curso';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-cursos',
	standalone: true,
	imports: [],
	templateUrl: './cursos.component.html',
	styleUrl: './cursos.component.css',
})
export class CursosComponent implements OnInit {
	constructor(
		public loginService: LoginService,
		private cdr: ChangeDetectorRef,
	) {
		this.loginService.usuario?.cursos_usuario.forEach((curso: Curso) => {
			console.log('LOG: ' + curso.nombre_curso);
		});
	}
	ngOnInit(): void {
		this.cdr.detectChanges();
	}
}
