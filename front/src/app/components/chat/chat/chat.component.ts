import { afterNextRender, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { CursoChat } from '../../../models/CursoChat';
import { MensajeChat } from '../../../models/MensajeChat';
import { ChatService } from '../../../services/chat.service';

@Component({
	selector: 'app-chat',
	standalone: true,
	imports: [],
	templateUrl: './chat.component.html',
	styleUrl: './chat.component.css',
})
export class ChatComponent implements OnInit, OnDestroy {
	@Input() idCurso: number | null = null;
	chat: CursoChat | null = null;
	respuesta: MensajeChat | null = null;
	respuestaClase: MensajeChat | null = null;
	subscription: Subscription | null = null;
	idMensaje: string | null = null;
	pregunta: { minute: number; second: number } | null = null;

	constructor(
		public chatService: ChatService,
		private route: ActivatedRoute,
		public cdr: ChangeDetectorRef,
	) {
		if (!this.idCurso) {
			this.route.paramMap.subscribe((params) => {
				this.idCurso = params.get('id_curso') as number | null;
				this.idMensaje = params.get('id_mensaje');
			});
		}
		afterNextRender(() => {
			console.log('AFTERNEXTRENDER');
			if (this.idCurso && this.idMensaje) {
				this.chatService.mensajeLeido(this.idMensaje);
			}
			if (this.idCurso) {
				this.subscription = this.chatService.getChat(this.idCurso).subscribe({
					next: (data: CursoChat | null) => {
						console.log('LLEGA LA RESPUESTA AL COMPONENTE: ', data);
						if (data) {
							this.chat = data;
							this.cdr.detectChanges();
							if (this.idMensaje) {
								if (data.mensajes.filter((mensaje) => mensaje.id_mensaje === this.idMensaje) && data.mensajes.filter((mensaje) => mensaje.id_mensaje === this.idMensaje).length > 0) {
									const mexc: HTMLElement | null = document.getElementById('mex-' + this.idMensaje);
									if (mexc) {
										mexc.scrollIntoView({ behavior: 'smooth', block: 'center' });
										mexc.focus();
									}
								} else {
									data.clases.forEach((clase) => {
										if (clase.mensajes.filter((mex) => mex.id_mensaje === this.idMensaje) && clase.mensajes.filter((mex) => mex.id_mensaje === this.idMensaje).length > 0) {
											this.abreChatClase(clase.id_clase);
											this.cdr.detectChanges();
											const mexc = document.getElementById('mex-' + this.idMensaje);
											console.log('MEXC2: ' + mexc);

											if (mexc) {
												mexc.scrollIntoView({ behavior: 'smooth', block: 'center' });
												mexc.focus();
												return;
											}
										}
									});
								}
							}
							this.cdr.detectChanges();
						}
					},
					error: (e) => {
						console.error('Error en recibir el chat: ' + e.message);
					},
				});
			} else {
				console.error('El curso es nulo');
			}
		});
	}

	ngOnInit(): void {
		if (!this.idCurso) {
			this.route.paramMap.subscribe((params) => {
				this.idCurso = params.get('id_curso') as number | null;
			});
		}
	}

	ngOnDestroy(): void {
		console.log('Se destruye el componente');
		this.idCurso = null;
		this.chat = null;
		this.respuesta = null;
		this.subscription?.unsubscribe();
	}

	enviarMensaje(clase?: number) {
		if (this.idCurso === null) {
			console.error('El curso es null');
			return;
		}
		if (clase) {
			let resp: string | null = null;
			if (this.respuestaClase) {
				resp = this.respuestaClase.id_mensaje;
			}
			const input: HTMLInputElement = document.getElementById('mexc-' + clase) as HTMLInputElement;
			if (input.value) {
				this.chatService.enviarMensaje(this.idCurso, clase, input.value, resp, this.pregunta);
				input.value = '';
				this.respuesta = null;
				this.respuestaClase = null;
				this.cdr.detectChanges();
			}
		} else {
			let resp: string | null = null;
			if (this.respuesta) {
				resp = this.respuesta.id_mensaje;
			}
			const input: HTMLInputElement = document.getElementById('mex') as HTMLInputElement;
			if (input.value) {
				// TODO gestionar la respuesta
				this.chatService.enviarMensaje(this.idCurso, 0, input.value, resp, this.pregunta);
				input.value = '';
				this.respuesta = null;
				this.respuestaClase = null;
				this.cdr.detectChanges();
			}
		}
	}

	abreChatClase(idClase: number) {
		if (!document.getElementById('clase-' + idClase)?.classList.contains('hidden')) {
			document.getElementById('clase-' + idClase)?.classList.add('hidden');
		} else {
			console.log('Se abre la clase ' + idClase);
			const clases: NodeListOf<Element> = document.querySelectorAll('.clases');
			for (let i = 0; i < clases.length; i++) {
				const clase: HTMLDivElement = clases.item(i) as HTMLDivElement;
				if (!clase.classList.contains('hidden')) {
					clase.classList.add('hidden');
				}
			}
			document.getElementById('clase-' + idClase)?.classList.remove('hidden');
		}
	}

	creaPregunta(idClase: number, momento: number) {
		this.abreChatClase(idClase);
		const minutes = Math.floor(momento / 60);
		const seconds = Math.floor(momento % 60);
		this.pregunta = { minute: minutes, second: seconds };
		console.log(`Clic en el tiempo: ${minutes}:${seconds}`);
		const input: HTMLInputElement = document.getElementById('mexc-' + idClase) as HTMLInputElement;
		input.placeholder = `Haz una pregunta en ${minutes}:${seconds}`;
		input.focus();
	}

	cierraPregunta(idClase: number) {
		this.respuesta = null;
		this.respuestaClase = null;
		this.pregunta = null;
		const input: HTMLInputElement = document.getElementById('mexc-' + idClase) as HTMLInputElement;
		input.placeholder = 'Escribe tu mensaje en la clase...';
		this.cdr.detectChanges();
	}
}
