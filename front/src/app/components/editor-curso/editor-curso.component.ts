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
	draggedElementId: number | null = null;
	constructor(
		public cursoService: CursosService,
		private route: ActivatedRoute,
	) {
		this.route.params.subscribe((params) => {
			this.id_curso = params['id_curso'];
			this.cursoService.getCurso(this.id_curso).then((curso) => (this.curso = curso));
		});
	}

	onDragStart(event: Event, id: number) {
		console.log('Start');
		const event2: DragEvent = event as DragEvent;
		const div = event2.target as HTMLDivElement;
		// Crear una copia del div original
		const img = div.cloneNode(true) as HTMLDivElement;

		// Copiar las clases del div original a la copia
		img.className = div.className;

		// Añadir la copia al documento de forma temporal para que setDragImage funcione
		img.style.position = 'absolute';
		img.style.top = '-9999px';
		document.body.appendChild(img);
		this.draggedElementId = id;
		event2.dataTransfer?.setData('text/plain', id.toString());
		event2.dataTransfer?.setDragImage(img, 0, 0);
		setTimeout(() => {
			document.body.removeChild(img);
		}, 0);
		div.classList.add('opacity-0');
	}

	onDragOver(event: Event, id: number) {
		event.preventDefault();
		const event2: DragEvent = event as DragEvent;
		console.log('Over ' + this.getClosestElementId(event2));
		if (this.draggedElementId === null || this.draggedElementId === id) {
			return;
		}

		if (this.curso?.clases_curso) {
			const draggedIndex = this.curso.clases_curso.findIndex((clase) => clase.id_clase === this.draggedElementId);
			const targetIndex = this.curso.clases_curso.findIndex((clase) => clase.id_clase === id);

			if (draggedIndex > -1 && targetIndex > -1 && draggedIndex !== targetIndex) {
				const draggedClase = this.curso.clases_curso[draggedIndex];
				const temp: number = draggedClase.posicion_clase;
				draggedClase.posicion_clase = this.curso.clases_curso[targetIndex].posicion_clase;
				this.curso.clases_curso[targetIndex].posicion_clase = temp;
				this.curso.clases_curso.splice(draggedIndex, 1);
				this.curso.clases_curso.splice(targetIndex, 0, draggedClase);
			}
		}
	}

	onDrop(event: Event) {
		event.preventDefault();
		this.draggedElementId = null;
	}

	getClosestElementId(event: DragEvent): number | null {
		const elements = Array.from(document.querySelectorAll('[id^="clase-"]'));
		const y = event.clientY;
		const closestElement = elements.reduce(
			(closest, element) => {
				const box = element.getBoundingClientRect();
				const offset = y - box.top - box.height / 2;
				if (offset < 0 && offset > closest.offset) {
					return { offset: offset, element };
				} else {
					return closest;
				}
			},
			{ offset: Number.NEGATIVE_INFINITY, element: null } as { offset: number; element: Element | null },
		).element;
		return closestElement ? parseInt(closestElement.id.split('-')[1], 10) : null;
	}

	onDragEnd(event: Event): void {
		const event2 = event as DragEvent;
		(event2.target as HTMLDivElement).classList.remove('opacity-0');
		this.draggedElementId = null;
	}
}
