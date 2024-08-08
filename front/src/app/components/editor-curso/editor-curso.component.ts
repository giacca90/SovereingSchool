import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { Clase } from '../../models/Clase';
import { Curso } from '../../models/Curso';
import { CursosService } from '../../services/cursos.service';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-editor-curso',
	standalone: true,
	imports: [FormsModule],
	templateUrl: './editor-curso.component.html',
	styleUrl: './editor-curso.component.css',
})
export class EditorCursoComponent implements OnInit, OnDestroy {
	private subscription: Subscription = new Subscription();
	id_curso: number = 0;
	curso: Curso | null = null;
	draggedElementId: number | null = null;
	editado: boolean = false;
	editar: Clase | null = null;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		public cursoService: CursosService,
		public loginService: LoginService,
	) {
		this.subscription.add(
			this.route.params.subscribe((params) => {
				this.id_curso = params['id_curso'];
				if (this.id_curso == 0) {
					if (this.loginService.usuario) {
						this.curso = new Curso(0, '', [this.loginService.usuario], '', '', new Date(), [], [], '', 0);
					}
				} else {
					this.cursoService.getCurso(this.id_curso).then((curso) => {
						this.curso = JSON.parse(JSON.stringify(curso));
					});
				}
			}),
		);
	}

	ngOnInit(): void {
		this.subscription.add(
			this.router.events.subscribe((event) => {
				if (event instanceof NavigationStart && this.editado) {
					this.cursoService.getCurso(this.id_curso).then((curso) => (this.curso = curso));

					const userConfirmed = window.confirm('Tienes cambios sin guardar. ¿Estás seguro de que quieres salir?');
					if (!userConfirmed) {
						this.router.navigateByUrl(this.router.url); // Mantén al usuario en la misma página
					}
				}
			}),
		);
	}
	ngOnDestroy(): void {
		this.subscription.unsubscribe();
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
		this.subscription.add(
			this.cursoService.updateCurso(this.curso).subscribe({
				next: (success: boolean) => {
					if (success) {
						this.editado = false;
					} else {
						console.error('Falló la actualización del curso');
					}
				},
				error: (error) => {
					console.error('Error al actualizar el curso:', error);
				},
			}),
		);
	}

	nuevaClase() {
		if (this.curso) this.editar = new Clase(0, '', '', '', 0, '', 0, this.curso.id_curso);
	}

	guardarCambios() {
		if (this.editar && this.curso) {
			this.editar.curso_clase = this.curso?.id_curso;
			if (this.editar?.id_clase === 0) {
				this.subscription.add(
					this.cursoService.createClass(this.editar).subscribe({
						next: (resp: boolean) => {
							if (resp && this.curso) {
								this.cursoService.getCurso(this.curso?.id_curso).then((response) => {
									if (response) {
										this.curso = response;
										this.editar = null;
									}
								});
							} else {
								console.error('Error en actualizar!!!');
							}
						},
						error: (e: Error) => {
							console.error('Error en crear la clase: ' + e.message);
						},
					}),
				);
			} else {
				this.subscription.add(
					this.cursoService.editClass(this.editar).subscribe({
						next: (resp: boolean) => {
							if (resp) {
								this.editar = null;
							} else {
								console.error('Error en actualizar!!!');
							}
						},
						error: (e: Error) => {
							console.error('Error en editar la clase: ' + e.message);
						},
					}),
				);
			}
		}
	}

	eliminaClase(clase: Clase) {
		if (confirm('Esto eliminará definitivamente la clase. Estás seguro??')) {
			clase.curso_clase = this.curso?.id_curso;
			this.subscription.add(
				this.cursoService.deleteClass(clase).subscribe({
					next: (resp: boolean) => {
						if (resp && this.curso) {
							this.cursoService.getCurso(this.curso?.id_curso).then((response) => {
								if (response) {
									this.curso = response;
								}
							});
						}
					},
					error: (e: Error) => {
						console.error('Error en eliminar Clase: ' + e.message);
					},
				}),
			);
		}
	}

	cargaVideo(event: Event) {
		const input = event.target as HTMLInputElement;
		if (!input.files) {
			return;
		}
		const reader = new FileReader();
		reader.onload = (e: ProgressEvent<FileReader>) => {
			if (e.target) {
				const vid: HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;
				vid.src = e.target.result as string;
				if (input.files)
					this.cursoService.subeVideo(input.files[0]).subscribe((result) => {
						console.log('Video subido: ' + result);
						if (result) {
							const edit: Clase = JSON.parse(JSON.stringify(this.editar));
							edit.curso_clase = this.curso?.id_curso;
							edit.direccion_clase = result;
							this.editar = JSON.parse(JSON.stringify(edit));
						}
					});
			}
		};
		reader.readAsDataURL(input.files[0]);
	}

	cargaImagenCurso(event: Event) {
		const input = event.target as HTMLInputElement;
		if (!input.files) {
			return;
		}
		const reader = new FileReader();
		reader.onload = (e: ProgressEvent<FileReader>) => {
			if (e.target && input.files && this.curso) {
				const formData = new FormData();
				formData.append('files', input.files[0], input.files[0].name);

				this.cursoService.addImagenCurso(formData).subscribe({
					next: (response) => {
						if (this.curso && response) this.curso.imagen_curso = response;
						this.cursoService.updateCurso(this.curso);
						this.editado = true;
					},
					error: (e: Error) => {
						console.error('Error en añadir la imagen al curso: ' + e.message);
					},
				});
			}
		};
		reader.readAsDataURL(input.files[0]);
	}
}
