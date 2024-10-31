import { isPlatformBrowser } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, Inject, OnDestroy, OnInit, PLATFORM_ID, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import videojs from 'video.js';
import { Clase } from '../../models/Clase';
import { Curso } from '../../models/Curso';
import { CursosService } from '../../services/cursos.service';
import { ChatComponent } from '../chat/chat/chat.component';

@Component({
	selector: 'app-reproduction',
	standalone: true,
	imports: [ChatComponent],
	templateUrl: './reproduction.component.html',
	styleUrl: './reproduction.component.css',
})
export class ReproductionComponent implements OnInit, AfterViewInit, OnDestroy {
	public id_usuario: number = 0;
	public id_curso: number = 0;
	public id_clase: number = 0;
	private isBrowser: boolean;
	private subscription: Subscription = new Subscription();
	public loading: boolean = true;
	public curso: Curso | null = null;
	public clase: Clase | null = null;
	@ViewChild(ChatComponent, { static: false }) chatComponent!: ChatComponent;

	constructor(
		private route: ActivatedRoute,
		private cdr: ChangeDetectorRef,
		@Inject(PLATFORM_ID) private platformId: object,
		public cursoService: CursosService,
		public router: Router,
	) {
		this.isBrowser = isPlatformBrowser(platformId);
	}

	ngOnInit(): void {
		this.subscription.add(
			this.route.params.subscribe((params) => {
				this.id_usuario = params['id_usuario'];
				this.id_curso = params['id_curso'];
				this.id_clase = params['id_clase'];
				if (this.id_clase == 0) {
					this.cursoService.getStatusCurso(this.id_usuario, this.id_curso).subscribe({
						next: (resp) => {
							if (resp === 0) {
								this.router.navigate(['/']);
							} else {
								this.router.navigate(['repro/' + this.id_usuario + '/' + this.id_curso + '/' + resp]);
							}
						},
					});
				} else {
					this.loadData();
				}
			}),
		);
	}

	loadData() {
		this.cursoService.getCurso(this.id_curso).then((result) => {
			this.curso = result;
			if (this.curso && this.curso.clases_curso) {
				const result = this.curso.clases_curso.find((clase) => clase.id_clase == this.id_clase);
				if (result) {
					this.clase = result;
				}
			}
		});
	}

	ngAfterViewInit(): void {
		if (this.isBrowser) {
			this.getVideo();
		}
	}

	private async getVideo() {
		try {
			const video: HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;

			if (video) {
				const player = videojs(video, {
					controls: true,
					autoplay: true,
					preload: 'auto',
				});
				player.src({
					src: `http://localhost:8090/${this.id_usuario}/${this.id_curso}/${this.id_clase}/master.m3u8`,
					type: 'application/x-mpegURL',
				});

				// Esperar a que el reproductor esté listo
				player.ready(() => {
					// Obtener el SeekBar del ProgressControl
					let seekBar = player.getChild('ControlBar');
					if (seekBar) {
						console.log('controlBar');
						seekBar = seekBar.getChild('ProgressControl');
						if (seekBar) {
							console.log('progressControl');
							seekBar = seekBar.getChild('SeekBar');
							if (seekBar) {
								console.log('seekBar');
								// Añadir el evento de clic derecho
								seekBar.on('contextmenu', (event: { preventDefault: () => void; clientX: number; clientY: number }) => {
									event.preventDefault(); // Evita el comportamiento predeterminado del clic
									if (seekBar) {
										const rect = seekBar.el().getBoundingClientRect();
										const clickPosition = event.clientX - rect.left;
										const clickRatio = clickPosition / rect.width;
										const duration = player.duration();
										if (duration) {
											const timeInSeconds = clickRatio * duration;
											this.muestraCortina(event.clientX, event.clientY, timeInSeconds);
										}
									}
								});
							}
						}
					}
				});

				this.loading = false;
				this.cdr.detectChanges();
			}
		} catch (error) {
			console.error('Error loading video:', error);
		}
	}

	ngOnDestroy(): void {
		this.subscription.unsubscribe();
	}

	navega(clase: Clase) {
		this.router.navigate(['/']).then(() => {
			this.router.navigate(['repro/' + this.id_usuario + '/' + this.id_curso + '/' + clase.id_clase]);
		});
	}

	cambiaVista(vista: number) {
		const vistaContenido: HTMLDivElement = document.getElementById('contenido') as HTMLDivElement;
		const vistaChat: HTMLDivElement = document.getElementById('chat') as HTMLDivElement;
		switch (vista) {
			case 0: {
				vistaContenido.hidden = false;
				vistaChat.hidden = true;
				break;
			}
			case 1: {
				vistaContenido.hidden = true;
				vistaChat.hidden = false;
				break;
			}
		}
	}

	// Función para mostrar la cortina en la posición del clic
	private muestraCortina(x: number, y: number, timeInSeconds: number) {
		const curtain = document.createElement('div');
		curtain.style.position = 'absolute';
		curtain.style.top = `${y}px`;
		curtain.style.left = `${x}px`;
		curtain.style.width = '200px';
		curtain.style.height = '100px';
		curtain.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
		curtain.style.color = 'white';
		curtain.style.padding = '10px';
		curtain.style.borderRadius = '5px';
		curtain.style.zIndex = '1000';

		// Botón para hacer una pregunta
		const pregunta: HTMLDivElement = document.createElement('div');
		pregunta.innerText = 'Haz una pregunta';
		pregunta.style.cursor = 'pointer';
		pregunta.addEventListener('click', () => {
			this.cambiaVista(1);
			this.chatComponent.creaPregunta(this.id_clase, timeInSeconds);
			document.body.removeChild(curtain);
		});

		curtain.appendChild(pregunta);
		document.body.appendChild(curtain);

		// Cierra la cortina al hacer clic fuera de ella
		window.addEventListener(
			'click',
			(event) => {
				if (!curtain.contains(event.target as Node)) {
					document.body.removeChild(curtain);
				}
			},
			{ once: true },
		);
	}
}
