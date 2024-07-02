import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Curso } from '../../models/Curso';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-cursos-usuario',
	standalone: true,
	imports: [],
	templateUrl: './cursos-usuario.component.html',
	styleUrl: './cursos-usuario.component.css',
})
export class CursosUsuarioComponent implements OnInit {
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
