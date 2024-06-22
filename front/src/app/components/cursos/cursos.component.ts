import { Component } from '@angular/core';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-cursos',
	standalone: true,
	imports: [],
	templateUrl: './cursos.component.html',
	styleUrl: './cursos.component.css',
})
export class CursosComponent {
	constructor(public loginService: LoginService) {}
}
