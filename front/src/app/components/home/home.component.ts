import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { Usuario } from '../../models/Usuario';
import { CursosService } from '../../services/cursos.service';
import { InitService } from '../../services/init.service';
import { UsuariosService } from '../../services/usuarios.service';

@Component({
	selector: 'app-home',
	standalone: true,
	imports: [],
	templateUrl: './home.component.html',
	styleUrl: './home.component.css',
})
export class HomeComponent {
	vistaCursos: HTMLDivElement[] = [];
	anchoVista: number = 0;
	vista: HTMLDivElement | null = null;
	constructor(
		public cursoService: CursosService,
		private usuarioService: UsuariosService,
		public initService: InitService,
		private cdr: ChangeDetectorRef,
		public router: Router,
	) {}

	async carouselProfes() {
		const carouselProfes: HTMLDivElement[] = [];
		this.usuarioService.profes.forEach((profe: Usuario) => {
			const div: HTMLDivElement = document.createElement('div') as HTMLDivElement;
			div.classList.add('border', 'border-black', 'rounded-lg', 'flex', 'h-full', 'p-2', 'flex-1', 'opacity-0', 'transition-opacity', 'duration-1000');

			const img: HTMLImageElement = document.createElement('img') as HTMLImageElement;
			img.classList.add('h-full', 'w-auto', 'object-contain', 'mr-4');
			img.src = profe.foto_usuario[0];
			img.alt = 'profe';
			div.appendChild(img);

			const desc: HTMLDivElement = document.createElement('div') as HTMLDivElement;
			desc.classList.add('flex', 'flex-col', 'flex-1', 'items-center', 'justify-center');

			const nombre: HTMLParagraphElement = document.createElement('p') as HTMLParagraphElement;
			nombre.classList.add('text-blond', 'text-green-700', 'text-center');
			nombre.textContent = profe.nombre_usuario.toString();
			desc.appendChild(nombre);

			const pres: HTMLParagraphElement = document.createElement('p') as HTMLParagraphElement;
			pres.classList.add('text-center');
			pres.textContent = profe.presentacion.toString();
			desc.appendChild(pres);

			div.appendChild(desc);
			carouselProfes.push(div);
		});

		const profes: HTMLDivElement = document.getElementById('profes') as HTMLDivElement;
		let reverse: boolean = false;
		while (carouselProfes.length > 0) {
			const profe: HTMLDivElement | undefined = carouselProfes.shift();
			if (profe) {
				profes.innerHTML = '';
				carouselProfes.push(profe);
				if (reverse) {
					profe.classList.add('flex-row-reverse');
				}
				profes.appendChild(profe);
				this.cdr.detectChanges();
				profe.classList.remove('opacity-0');
				reverse = !reverse;
				await this.delay(5000);
				profe.classList.add('opacity-0');
				await this.delay(1000);
			}
		}
	}

	delay(ms: number): Promise<void> {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}
}
