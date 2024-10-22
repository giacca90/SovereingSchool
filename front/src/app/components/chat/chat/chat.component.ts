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

	constructor(
		public chatService: ChatService,
		private route: ActivatedRoute,
		private cdr: ChangeDetectorRef,
	) {
		afterNextRender(() => {
			console.log('AFTERNEXTRENDER');
			if (this.idCurso) {
				this.subscription = this.chatService.getChat(this.idCurso).subscribe({
					next: (data: CursoChat | null) => {
						console.log('LLEGA LA RESPUESTA AL COMPONENTE: ', data);
						if (data) {
							this.chat = data;
							this.cdr.detectChanges();
							// TEST

							console.log('Curso: ' + this.chat);
							console.log('Clases: ' + this.chat.clases);
							this.chat.clases.forEach((clase) => {
								console.log('Clase: ' + clase);
							});
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
	ngOnDestroy(): void {
		console.log('Se destruye el componente');
		this.idCurso = null;
		this.chat = null;
		this.respuesta = null;
		this.subscription?.unsubscribe();
	}

	ngOnInit(): void {
		if (!this.idCurso) {
			this.route.paramMap.subscribe((params) => {
				this.idCurso = params.get('idCurso') as number | null;
			});
		}
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
			const input: HTMLInputElement = document.getElementById('mexc') as HTMLInputElement;
			if (input.value) {
				// TODO gestionar la respuesta
				this.chatService.enviarMensaje(this.idCurso, clase, input.value, resp);
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
				this.chatService.enviarMensaje(this.idCurso, 0, input.value, resp);
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
}
