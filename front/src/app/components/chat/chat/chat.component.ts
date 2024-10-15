import { afterNextRender, ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CursoChat } from '../../../models/CursoChat';
import { ChatService } from '../../../services/chat.service';

@Component({
	selector: 'app-chat',
	standalone: true,
	imports: [],
	templateUrl: './chat.component.html',
	styleUrl: './chat.component.css',
})
export class ChatComponent implements OnInit {
	@Input() idCurso: number | null = null;
	chat: CursoChat | null = null;

	constructor(
		public chatService: ChatService,
		private route: ActivatedRoute,
		private cdr: ChangeDetectorRef,
	) {
		afterNextRender(() => {
			console.log('AFTERNEXTRENDER');
			if (this.idCurso) {
				this.chatService.getChat(this.idCurso).subscribe({
					next: (data: CursoChat | null) => {
						console.log('LLEGA LA RESPUESTA AL COMPONENTE: ', data);
						if (data) {
							this.chat = data;
							this.cdr.detectChanges();
						}
					},
					error: (e) => {
						console.error('Error en recibir el chat: ' + e.message);
					},
				});
			}
		});
	}

	ngOnInit(): void {
		if (!this.idCurso) {
			this.route.paramMap.subscribe((params) => {
				this.idCurso = params.get('idCurso') as number | null;
			});
		}
	}

	enviarMensaje(clase: number) {
		if (this.idCurso === null) {
			console.error('El curso es null');
			return;
		}
		const input: HTMLInputElement = document.getElementById('mex') as HTMLInputElement;
		if (input.value) {
			this.chatService.enviarMensaje(this.idCurso, clase, input.value);
			input.value = '';
			this.cdr.detectChanges();
		}
	}
}
