import { isPlatformServer } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { Usuario } from '../models/Usuario';
import { CursosService } from '../services/cursos.service';

@Injectable({
	providedIn: 'root',
})
export class ProfGuard implements CanActivate {
	constructor(
		private cursoService: CursosService,
		private router: Router,
		@Inject(PLATFORM_ID) private platformId: object,
	) {}

	async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
		if (isPlatformServer(this.platformId)) return false;
		if (!localStorage.getItem('Usuario')) {
			this.router.navigate(['']);
			return false;
		}

		const usuario: Usuario = JSON.parse(localStorage.getItem('Usuario') as string);
		const id_curso = route.params['id_curso'];

		try {
			const curso = await this.cursoService.getCurso(id_curso);
			if (!curso) {
				this.router.navigate(['']);
				return false;
			}

			const isProfesor = curso.profesores_curso.some((profesor) => profesor.id_usuario === usuario.id_usuario);
			if (!isProfesor) {
				this.router.navigate(['']);
			}
			return isProfesor;
		} catch (error) {
			console.error('Error al obtener el curso:', error);
			this.router.navigate(['']);
			return false;
		}
	}
}
