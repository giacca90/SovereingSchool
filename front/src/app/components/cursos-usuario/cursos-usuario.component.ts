import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Usuario } from '../../models/Usuario';
import { CursosService } from '../../services/cursos.service';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-cursos-usuario',
	standalone: true,
	imports: [],
	templateUrl: './cursos-usuario.component.html',
	styleUrl: './cursos-usuario.component.css',
})
export class CursosUsuarioComponent implements OnInit, OnDestroy {
	usuario: Usuario | null = null;
	private subscription: Subscription = new Subscription();
	constructor(
		public loginService: LoginService,
		public cursoService: CursosService,
		private cdr: ChangeDetectorRef,
		public router: Router,
	) {}
	ngOnInit(): void {
		this.cdr.detectChanges();
		this.subscription.add(
			this.loginService.usuario$.subscribe((usuario) => {
				this.usuario = usuario;
			}),
		);
	}

	ngOnDestroy(): void {
		this.subscription.unsubscribe();
	}
}
