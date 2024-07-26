import { Component, HostListener, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { map } from 'rxjs';
import { Clase } from '../../models/Clase';
import { Curso } from '../../models/Curso';
import { CursosService } from '../../services/cursos.service';

@Component({
	selector: 'app-editor-curso',
	standalone: true,
	imports: [FormsModule],
	templateUrl: './editor-curso.component.html',
	styleUrl: './editor-curso.component.css',
})
export class EditorCursoComponent implements OnInit {
	id_curso: number = 0;
	curso: Curso | null = null;
	draggedElementId: number | null = null;
	editado: boolean = false;
	editar: Clase | null = null;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		public cursoService: CursosService,
	) {
		this.route.params.pipe(map((params) => (this.id_curso = params['id_curso'])));
		this.cursoService.getCurso(this.id_curso).then((curso) => {
			this.curso = JSON.parse(JSON.stringify(curso));
		});
	}

	ngOnInit(): void {
		this.router.events.pipe(
			map((event) => {
				if (event instanceof NavigationStart && this.editado) {
					this.cursoService.getCurso(this.id_curso).then((curso) => (this.curso = curso));

					const userConfirmed = window.confirm('Tienes cambios sin guardar. ¿Estás seguro de que quieres salir?');
					if (!userConfirmed) {
						this.router.navigateByUrl(this.router.url);
					}
				}
			}),
		);
	}

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: { returnValue: string }): void {
		if (this.editado) {
			$event.returnValue = 'Tienes cambios sin guardar. ¿Estás seguro de que quieres salir?';
		}
	}

	onDragStart(event: Event, id: number) {
		const event2: DragEvent = event as DragEvent;
		const div = event2.target as HTMLDivElement;
		const img = div.cloneNode(true) as HTMLDivElement;
		img.className = div.className;
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
		if (this.draggedElementId === null || this.draggedElementId === id) {
			return;
		}

		if (this.curso?.clases_curso) {
			const draggedIndex = this.curso.clases_curso.findIndex((clase) => clase.id_clase === this.draggedElementId);
			const targetIndex = this.curso.clases_curso.findIndex((clase) => clase.id_clase === id);

			if (draggedIndex > -1 && targetIndex > -1 && draggedIndex !== targetIndex) {
				const draggedClase = this.curso.clases_curso[draggedIndex];
				this.compruebaCambios();
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

	compruebaCambios() {
		this.cursoService.getCurso(this.id_curso).then((curso) => {
			this.editado = JSON.stringify(this.curso) !== JSON.stringify(curso);
		});
	}

	updateCurso() {
		this.cursoService
			.updateCurso(this.curso)
			.then((response: boolean) => {
				if (response) {
					this.editado = false;
				} else {
					console.error('Falló la actualización del curso');
				}
			})
			.catch((error: Error) => {
				console.error('Error en actualizar el curso: ' + error.message);
			});
	}

	nuevaClase() {
		if (this.curso) this.editar = new Clase(0, '', '', '', 0, '', 0, this.curso.id_curso);
	}

	guardarCambios() {
		if (this.editar && this.curso) {
			this.editar.curso_clase = this.curso?.id_curso;
			if (this.editar?.id_clase === 0) {
				this.cursoService
					.createClass(this.editar)
					.then((response: boolean) => {
						if (response && this.curso) {
							this.cursoService.getCurso(this.curso?.id_curso).then((response) => {
								if (response) {
									this.curso = response;
									this.editar = null;
								}
							});
						} else {
							console.error('Error en actualizar!!!');
						}
					})
					.catch((error: Error) => {
						console.error('Error en crear la clase: ' + error.message);
					});
			} else {
				this.cursoService
					.editClass(this.editar)
					.then((response: boolean) => {
						if (response) {
							this.editar = null;
						} else {
							console.error('Error en actualizar!!!');
						}
					})
					.catch((error: Error) => {
						console.error('Error en editar la clase: ' + error.message);
					});
			}
		}
	}

	eliminaClase(clase: Clase) {
		if (confirm('Esto eliminará definitivamente la clase. Estás seguro??')) {
			clase.curso_clase = this.curso?.id_curso;
			this.cursoService
				.deleteClass(clase)
				.then((response: boolean) => {
					if (response && this.curso) {
						this.cursoService.getCurso(this.curso?.id_curso).then((response) => {
							if (response) {
								this.curso = response;
							}
						});
					}
				})
				.catch((error: Error) => {
					console.error('Error en eliminar Clase: ' + error.message);
				});
		}
	}
}
