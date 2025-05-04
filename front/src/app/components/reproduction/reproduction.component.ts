import { isPlatformBrowser } from '@angular/common';
import { AfterViewInit, ChangeDetectorRef, Component, Inject, OnDestroy, OnInit, PLATFORM_ID, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import Player from 'video.js/dist/types/player';
import { Clase } from '../../models/Clase';
import { ClaseChat } from '../../models/ClaseChat';
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
	public momento: number | null = null;
	private isBrowser: boolean;
	private subscription: Subscription = new Subscription();
	public loading: boolean = true;
	public curso: Curso | null = null;
	public clase: Clase | null = null;
	@ViewChild(ChatComponent, { static: false }) chatComponent!: ChatComponent;
	backStream: string = '';

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
				this.momento = params['momento'] || null;

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
		this.subscription.add(
			this.route.queryParams.subscribe((qparams) => {
				this.momento = qparams['momento'] || null;
			}),
		);
		if (isPlatformBrowser(this.platformId)) {
			this.backStream = (window as any).__env?.BACK_STREAM ?? '';
		}
	}

	loadData() {
		this.cursoService.getCurso(this.id_curso).then((result) => {
			this.curso = result;
			if (this.curso && this.curso.clases_curso) {
				const result = this.curso.clases_curso.find((clase) => clase.id_clase == this.id_clase);
				if (result) {
					this.clase = result;
					this.cdr.detectChanges();
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
		if (!this.isBrowser) return; // 🛡️ Protección SSR

		try {
			const video: HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;
			if (!video) {
				console.warn('Elemento de video no encontrado');
				return;
			}

			// 📦 Importación dinámica
			const videojsModule = await import('video.js');
			const videojs = videojsModule.default;

			const player = videojs(video, {
				controls: true,
				autoplay: true,
				preload: 'auto',
				techOrder: ['html5'],
				html5: {
					vhs: {
						withCredentials: true,
					},
				},
			});

			player.src({
				src: `${this.backStream}/${this.id_usuario}/${this.id_curso}/${this.id_clase}/master.m3u8`,
				type: 'application/x-mpegURL',
				withCredentials: true,
			});

			player.ready(() => {
				const controlBar = player.getChild('ControlBar');
				const progressControl = controlBar?.getChild('ProgressControl');
				const seekBar = progressControl?.getChild('SeekBar');

				if (seekBar) {
					seekBar.on('contextmenu', (event: MouseEvent) => {
						event.preventDefault();
						const rect = seekBar.el().getBoundingClientRect();
						const clickPosition = event.clientX - rect.left;
						const clickRatio = clickPosition / rect.width;
						const duration = player.duration();

						if (duration) {
							const timeInSeconds = clickRatio * duration;
							this.muestraCortina(event.clientX, event.clientY, timeInSeconds);
						}
					});
				}

				this.loading = false;

				if (this.momento) {
					player.currentTime(this.momento > 3 ? this.momento - 3 : this.momento);
				}

				this.cdr.detectChanges();
			});

			player.on('play', () => {
				this.esperarChatComponent(player);
			});
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

	async esperarChatComponent(player: Player) {
		// Si `chat` no está cargado, espera un segundo antes de continuar
		while (!this.chatComponent.chat) {
			console.log('CHAT NO CARGADO');
			await new Promise((resolve) => setTimeout(resolve, 300)); // Espera 0.3 segundos
		}

		// Ejecutar el código después de verificar que `chat` está definido
		console.log('CHAT CARGADO');
		console.log('this.id_clase: ' + this.id_clase);
		const claseChat: ClaseChat | undefined = this.chatComponent.chat?.clases.find((clase) => clase.id_clase == this.id_clase);
		if (claseChat) {
			console.log('CLASE CHAT: ' + claseChat);
			claseChat.mensajes
				.filter((mex) => mex.pregunta)
				.forEach((preg) => {
					console.log('PREG: ' + preg);
					if (preg.pregunta) {
						const duration = player.duration(); // Duración total del video en segundos
						const preguntaTime = preg.pregunta; // Tiempo de la pregunta en segundos
						if (duration && preguntaTime <= duration) {
							const clickRatio = preguntaTime / duration; // Ratio del tiempo de la pregunta respecto a la duración
							const seekBar = player.getChild('ControlBar')?.getChild('ProgressControl')?.getChild('SeekBar');
							if (seekBar) {
								const rect = seekBar.el().getBoundingClientRect();
								const preguntaPosX = rect.width * clickRatio; // Posición en píxeles en la barra de progreso

								console.log(`Pregunta en el tiempo: ${preguntaTime} segundos, Posición en la barra: ${preguntaPosX}px`);

								// Crear el marcador como un div
								const marcador = document.createElement('div');
								marcador.style.position = 'absolute';
								marcador.style.left = `${preguntaPosX}px`; // Posición en la barra
								marcador.style.top = '0';
								marcador.style.width = '4px';
								marcador.style.height = '100%';
								marcador.style.zIndex = '10';
								marcador.style.backgroundColor = '#eab308';
								// Evento para mostrar la cortina al pasar el ratón
								let overCortina: boolean = false;
								marcador.addEventListener('mouseover', (event: MouseEvent) => {
									const cortina = document.createElement('div');
									cortina.className = 'cortina-info';
									cortina.innerText = preg.mensaje ? preg.mensaje : '';
									cortina.style.position = 'absolute';
									cortina.style.left = `${event.clientX}px`; // Posición en X según el ratón
									cortina.style.top = `${event.clientY + 20}px`; // Posición en Y ajustada ligeramente para aparecer debajo del ratón
									cortina.style.padding = '5px';
									cortina.style.zIndex = '10';
									cortina.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
									cortina.style.color = 'white';
									cortina.style.borderRadius = '3px';
									cortina.style.zIndex = '1000';
									cortina.style.cursor = 'pointer';
									cortina.addEventListener('mouseout', () => {
										document.body.removeChild(cortina);
										overCortina = false;
									});
									cortina.addEventListener('mouseover', () => (overCortina = true));

									// TODO: acabar navegación al mensaje
									cortina.addEventListener('click', () => {
										this.cambiaVista(1);
										this.chatComponent.abreChatClase(this.id_clase);
										document.body.removeChild(cortina);
										const mensajeElement = document.getElementById('mex-' + preg.id_mensaje);
										if (mensajeElement) {
											mensajeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
										} else {
											console.log('No se encontró el mensaje');
										}
									});

									// Agregar la cortina al documento
									document.body.appendChild(cortina);

									// Evento para ocultar la cortina cuando el ratón sale del marcador
									marcador.addEventListener(
										'mouseout',
										() => {
											setTimeout(() => {
												if (!overCortina) {
													document.body.removeChild(cortina);
												}
											}, 1000);
										},
										{ once: true },
									);
								});
								// Añadir el marcador al SeekBar
								seekBar.el().appendChild(marcador);
							}
						}
					}
				});
		}
		this.cdr.detectChanges();
	}
}
