import { isPlatformBrowser } from '@angular/common';
import { AfterViewInit, Component, HostListener, Inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import Player from 'video.js/dist/types/player';
import { Clase } from '../../models/Clase';
import { Curso } from '../../models/Curso';
import { CursosService } from '../../services/cursos.service';
import { InitService } from '../../services/init.service';
import { LoginService } from '../../services/login.service';
import { StreamingService } from '../../services/streaming.service';
import { EditorWebcamComponent, VideoElement } from '../editor-curso/editor-webcam/editor-webcam.component';

@Component({
	selector: 'app-editor-curso',
	standalone: true,
	imports: [FormsModule, EditorWebcamComponent],
	templateUrl: './editor-curso.component.html',
	styleUrl: './editor-curso.component.css',
})
export class EditorCursoComponent implements OnInit, OnDestroy, AfterViewInit {
	private subscription: Subscription = new Subscription();
	id_curso: number = 0;
	curso: Curso | null = null;
	draggedElementId: number | null = null;
	editado: boolean = false;
	editar: Clase | null = null;
	streamWebcam: MediaStream | null = null;
	m3u8Loaded: boolean = false;
	player: Player | null = null;
	ready: Subject<boolean> = new Subject<boolean>();
	savedFiles: File[] = [];
	savedPresets: Map<string, { elements: VideoElement[]; shortcut: string }> = new Map();
	backBase = '';
	isBrowser: boolean = false;
	videojs: any;

	constructor(
		private route: ActivatedRoute,
		private router: Router,
		public cursoService: CursosService,
		public loginService: LoginService,
		public streamingService: StreamingService,
		private initService: InitService,
		@Inject(PLATFORM_ID) private platformId: Object,
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
						// Si el curso no existe, redirige a la página de inicio
						if (!this.curso) {
							this.router.navigate(['/']);
						}
					});
				}
			}),
		);

		this.isBrowser = isPlatformBrowser(platformId);
	}
	ngAfterViewInit(): void {
		if (this.isBrowser) {
			this.videojs = require('video.js'); // 👈 importante: cargarlo dinámicamente
		}
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

		if (isPlatformBrowser(this.platformId)) {
			this.backBase = (window as any).__env?.BACK_BASE ?? '';
		}
	}

	ngOnDestroy(): void {
		this.subscription.unsubscribe();
		this.streamWebcam?.getTracks().forEach((track) => track.stop());
		this.streamWebcam = null;
	}

	@HostListener('window:beforeunload', ['$event'])
	unloadNotification($event: { returnValue: string }): void {
		if (!this.isBrowser) return;

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
						this.router.navigate(['/cursosUsuario']);
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
		if (this.curso && this.curso.clases_curso) {
			this.editar = new Clase(0, '', '', '', 0, '', this.curso.clases_curso?.length + 1, this.curso.id_curso);
		}

		// Recuperar imagenes del curso y del usuario para el componente WebOBS
		if (this.curso && this.curso.imagen_curso) {
			fetch(this.curso.imagen_curso, { credentials: 'include' }).then((response) => {
				response.blob().then((blob) => {
					if (!this.curso) return;
					const fileName = this.curso.imagen_curso.split('/').pop();
					// Detectar el tipo MIME del Blob
					const mimeType = blob.type || 'application/octet-stream';
					if (fileName) {
						const test = this.savedFiles?.find((file) => file.name === fileName);
						if (!test) {
							const file = new File([blob], fileName, { type: mimeType });
							this.savedFiles?.push(file);
						}
					}
				});
			});
		}

		this.loginService.usuario?.foto_usuario.forEach((foto) => {
			console.log('Foto del usuario:', foto);
			fetch(foto, { credentials: 'include' }).then((response) => {
				response.blob().then((blob) => {
					const fileName = foto.split('/').pop();
					const mimeType = blob.type || 'application/octet-stream';
					if (fileName) {
						const test = this.savedFiles?.find((file) => file.name === fileName);
						if (!test) {
							const file = new File([blob], fileName, { type: mimeType });
							this.savedFiles?.push(file);
						}
					}
				});
			});
		});

		// Recuperar los presets del usuario
		this.streamingService.getPresets().subscribe((res) => {
			try {
				console.log('Presets recibidos:', res);
				this.savedPresets = new Map(Object.entries(res));
			} catch (error) {
				console.error('Error al parsear los presets:', error);
			}
		});

		this.cambiaTipoClase(0);
	}

	guardarCambiosClase() {
		if (this.curso && this.editar) {
			console.log('Tipo de clase: ' + this.editar.tipo_clase);
			if (this.editar.tipo_clase > 0) {
				this.cursoService.getCurso(this.curso.id_curso, true).then((curso) => {
					this.curso = curso;
					this.editar = null;
				});
			} else if (this.editar.id_clase === 0) {
				if (this.editar) {
					if (this.editar.nombre_clase == null || this.editar.nombre_clase == '') {
						alert('Debes poner un nombre para la clase');
						return;
					}
					if (this.editar.descriccion_clase == null || this.editar.descriccion_clase == '') {
						alert('Debes poner una descripción para la clase');
						return;
					}
					if (this.editar.contenido_clase == null || this.editar.contenido_clase == '') {
						alert('Debes poner contenido para la clase');
						return;
					}
				}
				this.curso.clases_curso?.push(this.editar);
				this.editar = null;
			} else {
				this.editar = null;
			}
		}
		this.streamWebcam?.getTracks().forEach((track) => track.stop());
		this.streamWebcam = null;
		this.player?.dispose();
		document.body.style.overflow = 'auto';
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
							this.router.navigate(['/cursosUsuario']);
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

	cambiaTipoClase(tipo: number) {
		this.streamingService.closeConnections();
		setTimeout(() => {
			if (!this.editar) return;
			const videoButton: HTMLButtonElement = document.getElementById('claseVideo') as HTMLButtonElement;
			const obsButton: HTMLButtonElement = document.getElementById('claseOBS') as HTMLButtonElement;
			const webcamButton: HTMLButtonElement = document.getElementById('claseWebCam') as HTMLButtonElement;
			if (videoButton) {
				videoButton.classList.remove('text-blue-700');
			}
			if (obsButton) {
				obsButton.classList.remove('text-blue-700');
			}
			if (webcamButton) {
				webcamButton.classList.remove('text-blue-700');
			}
			window.scrollTo(0, 0); // Subir la vista al inicio de la página
			document.body.style.overflow = 'hidden';

			this.player?.dispose();
			this.player = null;

			switch (tipo) {
				case 0: {
					this.editar.tipo_clase = 0;
					videoButton.classList.add('text-blue-700');
					this.streamWebcam?.getTracks().forEach((track) => track.stop());
					this.streamWebcam = null;
					break;
				}
				case 1: {
					this.editar.tipo_clase = 1;
					obsButton.classList.add('text-blue-700');
					this.streamWebcam?.getTracks().forEach((track) => track.stop());
					this.streamWebcam = null;
					this.startOBS();
					break;
				}
				case 2: {
					this.editar.tipo_clase = 2;
					webcamButton.classList.add('text-blue-700');
					this.streamWebcam?.getTracks().forEach((track) => track.stop());
					this.streamWebcam = null;
					break;
				}
			}
		}, 100);
	}

	async startVideoJS() {
		if (!this.isBrowser || !this.editar) return;

		// Dinámicamente importa video.js solo en el navegador
		const videojsModule = await import('video.js');
		const videojs = videojsModule.default;

		window.scrollTo(0, 0);
		document.body.style.overflow = 'hidden';

		const videoPlayer = document.getElementById('videoPlayer') as HTMLVideoElement;

		if (videoPlayer && this.editar.direccion_clase && this.editar.direccion_clase.endsWith('.m3u8')) {
			this.player = videojs(videoPlayer, {
				aspectRatio: '16:9',
				controls: true,
				autoplay: false,
				preload: 'auto',
			});

			this.player.src({
				src: `${this.backBase}/${this.loginService.usuario?.id_usuario}/${this.curso?.id_curso}/${this.editar.id_clase}/master.m3u8`,
				type: 'application/x-mpegURL',
				withCredentials: true,
			});
		} else {
			console.error('No se pudo obtener video.js');
		}
	}
	visualizeAudio(stream: MediaStream, audioLevel: HTMLDivElement) {
		const audioContext = new AudioContext();
		const analyser = audioContext.createAnalyser();
		const source = audioContext.createMediaStreamSource(stream);

		source.connect(analyser);

		analyser.fftSize = 256; // Ajusta la resolución de frecuencia
		const dataArray = new Uint8Array(analyser.frequencyBinCount);

		function updateAudioLevel() {
			analyser.getByteFrequencyData(dataArray);
			const volume = Math.max(...dataArray) / 255; // Escalar de 0 a 1
			const percentage = Math.min(volume * 100, 100); // Limitar a 100%
			audioLevel.style.width = `${percentage}%`; // Ajustar ancho de la barra

			requestAnimationFrame(updateAudioLevel); // Continuar la animación
		}

		updateAudioLevel(); // Iniciar la visualización
	}

	emiteWebcam(event: MediaStream | null) {
		if (this.editar) {
			if (event === null) {
				this.detenerEmision();
				this.ready.next(false);
				return;
			}
			this.streamWebcam = event;
			if (this.editar.nombre_clase == null || this.editar.nombre_clase == '') {
				alert('Debes poner un nombre para la clase');
				this.ready.next(false);
				return;
			}
			if (this.editar.descriccion_clase == null || this.editar.descriccion_clase == '') {
				alert('Debes poner una descripción para la clase');
				this.ready.next(false);
				return;
			}
			if (this.editar.contenido_clase == null || this.editar.contenido_clase == '') {
				alert('Debes poner contenido para la clase');
				this.ready.next(false);
				return;
			}
			if (!this.streamWebcam) {
				alert('Debes conectarte primero con la webcam');
				this.ready.next(false);
				return;
			} else {
				this.ready.next(true);
				this.streamingService.emitirWebcam(this.streamWebcam, this.editar);
				this.editado = true;
			}
		}
	}

	async startOBS() {
		if (!this.isBrowser) return; // ✅ Evita ejecutar en SSR

		const userId = this.loginService.usuario?.id_usuario;
		if (!userId) return;

		this.streamingService.startOBS(userId);

		// Esperar a que el DOM esté listo
		setTimeout(async () => {
			const videoOBS = document.getElementById('OBS') as HTMLVideoElement;
			if (!videoOBS) {
				console.error('Elemento con ID "OBS" no encontrado');
				return;
			}

			// ✅ Importar dinámicamente video.js
			const videojsModule = await import('video.js');
			const videojs = videojsModule.default;

			this.player = videojs(videoOBS, {
				aspectRatio: '16:9',
				controls: false,
				autoplay: true,
				preload: 'auto',
				html5: {
					hls: {
						overrideNative: true,
						enableLowLatency: true,
					},
					vhs: {
						lowLatencyMode: true,
					},
				},
				liveui: true,
			});

			this.player.src({
				src: this.streamingService.UrlPreview,
				type: 'application/x-mpegURL',
				withCredentials: true,
			});

			this.player.on('loadeddata', () => {
				console.log('Archivo .m3u8 cargado correctamente');
				this.m3u8Loaded = true;

				const techEl = this.player?.tech(true)?.el() as HTMLVideoElement & { captureStream(): MediaStream };
				if (techEl?.captureStream) {
					const mediaStream = techEl.captureStream();
					const audioLevel = document.getElementById('audio-level') as HTMLDivElement;
					if (audioLevel) {
						this.visualizeAudio(mediaStream, audioLevel);
					}
				}
			});

			// ✅ Asegura el estilo solo si el elemento existe
			videoOBS.style.height = 'auto'; // Usar 'auto' en lugar de 'content' (no válido en CSS)
		}, 300);
	}

	emiteOBS() {
		if (this.editar) {
			if (this.editar.nombre_clase == null || this.editar.nombre_clase == '') {
				alert('Debes poner un nombre para la clase');
				return;
			}
			if (this.editar.descriccion_clase == null || this.editar.descriccion_clase == '') {
				alert('Debes poner una descripción para la clase');
				return;
			}
			if (this.editar.contenido_clase == null || this.editar.contenido_clase == '') {
				alert('Debes poner contenido para la clase');
				return;
			}
			if (!this.m3u8Loaded) {
				alert('Debes conectarte primero con OBS');
				return;
			}
		}
		this.editado = true;
		this.streamingService.emitirOBS(this.editar);
	}

	detenerEmision() {
		this.streamingService.stopMediaStreaming();
	}

	savePresets(data: any) {
		this.streamingService.savePresets(data);
	}
}
