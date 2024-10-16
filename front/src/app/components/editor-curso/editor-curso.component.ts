import { AfterViewChecked, Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import videojs from 'video.js';
import { Clase } from '../../models/Clase';
import { Curso } from '../../models/Curso';
import { Usuario } from '../../models/Usuario';
import { CursosService } from '../../services/cursos.service';
import { InitService } from '../../services/init.service';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-editor-curso',
	standalone: true,
	imports: [FormsModule],
	templateUrl: './editor-curso.component.html',
	styleUrl: './editor-curso.component.css',
})
export class EditorCursoComponent implements OnInit, OnDestroy, AfterViewChecked {
	private subscription: Subscription = new Subscription();
	id_curso: number = 0;
	curso: Curso | null = null;
	draggedElementId: number | null = null;
	editado: boolean = false;
	editar: Clase | null = null;
	videoPlayer: HTMLVideoElement | null = null;
	usuario: Usuario | null = null;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		public cursoService: CursosService,
		public loginService: LoginService,
		private initService: InitService,
	) {
		this.subscription.add(
			this.loginService.usuario$.subscribe((usuario) => {
				this.usuario = usuario;
			}),
		);

		this.subscription.add(
			this.route.params.subscribe((params) => {
				this.id_curso = params['id_curso'];
				if (this.id_curso == 0) {
					if (this.usuario) {
						this.curso = new Curso(0, '', [this.usuario], '', '', new Date(), [], [], '', 0);
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
						this.initService.carga();
						this.editado = false;
						this.router.navigate(['cursosUsuario']);
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
		if (this.curso && this.curso.clases_curso) this.editar = new Clase(0, '', '', '', 0, '', this.curso.clases_curso?.length + 1, this.curso.id_curso);
	}

	guardarCambiosClase() {
		if (this.curso && this.editar) {
			this.curso.clases_curso?.push(this.editar);
			this.editar = null;
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
									this.initService.carga();
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
			alert('Sube un video valido!!!');
			return;
		}
		const button = document.getElementById('video-upload-button') as HTMLSpanElement;
		button.classList.remove('border-black');
		button.classList.add('border-gray-500', 'text-gray-500');
		const button_guardar_clase = document.getElementById('button-guardar-clase') as HTMLButtonElement;
		button_guardar_clase.classList.remove('border-black');
		button_guardar_clase.classList.add('border-gray-500', 'text-gray-500');
		button_guardar_clase.disabled = true;

		const reader = new FileReader();
		reader.onload = (e: ProgressEvent<FileReader>) => {
			if (e.target) {
				const vid: HTMLVideoElement = document.getElementById('videoPlayer') as HTMLVideoElement;
				vid.src = e.target.result as string;
				if (this.editar && !this.editar?.id_clase) {
					this.editar.id_clase = 0;
				}
				if (input.files && this.editar) {
					this.cursoService.subeVideo(input.files[0], this.id_curso, this.editar?.id_clase).subscribe((result) => {
						if (result && this.curso?.clases_curso && this.editar) {
							this.editar.direccion_clase = result;
							this.editar.curso_clase = this.curso.id_curso;
							button.classList.remove('border-gray-500', 'text-gray-500');
							button.classList.add('border-black');
							button_guardar_clase.classList.remove('border-gray-500', 'text-gray-500');
							button_guardar_clase.classList.add('border-black');
							button_guardar_clase.disabled = false;
							this.editado = true;
						}
					});
				}
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
						//						this.cursoService.updateCurso(this.curso);
						this.compruebaCambios();
					},
					error: (e: Error) => {
						console.error('Error en añadir la imagen al curso: ' + e.message);
					},
				});
			}
		};
		reader.readAsDataURL(input.files[0]);
	}

	deleteCurso() {
		const confirm = window.confirm('Esta acción borrará definitivamente este curso, incluida todas sus clases con su contenido. \n Tampoco el administrador de la plataforma podrá recuperar el curso una vez borrado.');
		if (confirm) {
			const confirm2 = window.confirm('ESTÁS ABSOLUTAMENTE SEGURO DE LO QUE HACES??');
			if (confirm2 && this.curso) {
				this.cursoService.deleteCurso(this.curso).subscribe({
					next: (result: boolean) => {
						if (result) {
							this.initService.carga();
							this.router.navigate(['cursosUsuario']);
						}
					},
					error: (e: Error) => {
						console.error('Error en eliminar el curso: ' + e.message);
					},
				});
			}
		}
	}

	keyEvent(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			document.getElementById('video-upload')?.click();
		}
	}

	ngAfterViewChecked(): void {
		if (this.editar && !this.videoPlayer) {
			this.videoPlayer = document.getElementById('videoPlayer') as HTMLVideoElement;
			if (this.videoPlayer && this.editar.direccion_clase && this.editar.direccion_clase.length > 0) {
				const player = videojs(this.videoPlayer, {
					controls: true,
					autoplay: false,
					preload: 'auto',
				});
				player.src({
					src: `http://localhost:8090/${this.usuario?.id_usuario}/${this.curso?.id_curso}/${this.editar.id_clase}/master.m3u8`,
					type: 'application/x-mpegURL',
				});
			}
		}
	}
}
