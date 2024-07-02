import { afterNextRender, ChangeDetectorRef, Component, OnInit } from '@angular/core';
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
export class HomeComponent implements OnInit {
	vistaCursos: HTMLDivElement[] = [];
	anchoVista: number = 0;
	vista: HTMLDivElement | null = null;
	constructor(
		private cursoService: CursosService,
		private usuarioService: UsuariosService,
		private cdr: ChangeDetectorRef,
	) {
		this.cursoService.cursos.forEach((curso: Curso) => {
			let div: HTMLDivElement = document.createElement('div');
			div.classList.add('relative', 'm-2', 'border', 'border-black');

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
		});
	}

	ngOnInit(): void {
		/* let numeroVistas: number;
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

		this.cdr.detectChanges(); */
	}
}
