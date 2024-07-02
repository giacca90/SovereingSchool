import { afterNextRender, ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { Curso } from '../../models/Curso';
import { Usuario } from '../../models/Usuario';
import { CursosService } from '../../services/cursos.service';
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
		private cursoService: CursosService,
		private usuarioService: UsuariosService,
		private cdr: ChangeDetectorRef,
		public router: Router,
	) {
		this.cursoService.cursos.forEach((curso: Curso) => {
			let div: HTMLDivElement = document.createElement('div');
			div.classList.add('relative', 'm-2', 'border', 'border-black', 'cursor-pointer');

			let fondo: HTMLDivElement = document.createElement('div');
			fondo.classList.add('absolute', 'inset-0', 'bg-cover', 'bg-center', 'opacity-20', 'transition-opacity', 'duration-300', 'hover:opacity-30');
			fondo.style.backgroundImage = `url('${curso.imagen_curso.substring(curso.imagen_curso.indexOf('/assets'))}')`;
			div.appendChild(fondo);

			let nombre: HTMLHeadingElement = document.createElement('h4');
			nombre.classList.add('text-center', 'font-bold', 'text-blue-700');
			nombre.textContent = curso.nombre_curso;
			div.appendChild(nombre);

			curso.profesores_curso.forEach((profe: Usuario) => {
				let divProfe: HTMLDivElement = document.createElement('div');
				divProfe.classList.add('m-3', 'flex', 'items-center', 'justify-between');
				let span: HTMLSpanElement = document.createElement('span');
				span.textContent = 'Por: ' + profe.nombre_usuario;
				divProfe.appendChild(span);
				let logo: HTMLImageElement = document.createElement('img') as HTMLImageElement;
				logo.classList.add('h-12', 'w-12', 'object-contain');
				logo.src = profe.foto_usuario[0].substring(profe.foto_usuario[0].indexOf('/assets'));
				logo.alt = 'Logo';
				divProfe.appendChild(logo);
				div.appendChild(divProfe);
			});

			let desc: HTMLParagraphElement = document.createElement('p');
			desc.classList.add('ml-3', 'mr-3');
			desc.textContent = curso.descriccion_corta as string;
			div.appendChild(desc);

			div.addEventListener('click', () => {
				this.router.navigate(['/curso/' + curso.id_curso]);
			});

			this.vistaCursos.push(div);
		});

		afterNextRender(() => {
			this.anchoVista = window.innerWidth;
			this.vista = document.getElementById('carousel') as HTMLDivElement;
			let numeroVistas: number;
			if (this.anchoVista < 640) {
				numeroVistas = 2;
			} else if (this.anchoVista >= 640 && this.anchoVista < 768) {
				numeroVistas = 3;
			} else if (this.anchoVista >= 768 && this.anchoVista < 1024) {
				numeroVistas = 4;
			} else if (this.anchoVista >= 1024 && this.anchoVista < 1280) {
				numeroVistas = 4;
			} else if (this.anchoVista >= 1280 && this.anchoVista < 1536) {
				numeroVistas = 5;
			} else {
				numeroVistas = 6;
			}

			let numeroRealVistas = numeroVistas;
			if (this.cursoService.cursos.length < numeroVistas) numeroRealVistas = this.cursoService.cursos.length;

			for (let i = 0; i < numeroRealVistas; i++) {
				this.vista?.appendChild(this.vistaCursos[i]);
			}

			this.cdr.detectChanges();
			this.carouselProfes();
		});
	}

	async carouselProfes() {
		let carouselProfes: HTMLDivElement[] = [];
		this.usuarioService.profes.forEach((profe: Usuario) => {
			let div: HTMLDivElement = document.createElement('div') as HTMLDivElement;
			div.classList.add('border', 'border-black', 'rounded-lg', 'flex', 'h-full', 'p-2', 'flex-1', 'opacity-0', 'transition-opacity', 'duration-1000');

			let img: HTMLImageElement = document.createElement('img') as HTMLImageElement;
			img.classList.add('h-full', 'w-auto', 'object-contain', 'mr-4');
			img.src = profe.foto_usuario[0].substring(profe.foto_usuario[0].indexOf('/assets'));
			div.appendChild(img);

			let desc: HTMLDivElement = document.createElement('div') as HTMLDivElement;
			desc.classList.add('flex', 'flex-col', 'flex-1', 'items-center', 'justify-center');

			let nombre: HTMLParagraphElement = document.createElement('p') as HTMLParagraphElement;
			nombre.classList.add('text-blond', 'text-green-700', 'text-center');
			nombre.textContent = profe.nombre_usuario.toString();
			desc.appendChild(nombre);

			let pres: HTMLParagraphElement = document.createElement('p') as HTMLParagraphElement;
			pres.classList.add('text-center');
			pres.textContent = profe.presentacion.toString();
			desc.appendChild(pres);

			div.appendChild(desc);
			carouselProfes.push(div);
		});

		let profes: HTMLDivElement = document.getElementById('profes') as HTMLDivElement;
		let reverse: boolean = false;
		while (carouselProfes.length > 0) {
			let profe: HTMLDivElement | undefined = carouselProfes.shift();
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
