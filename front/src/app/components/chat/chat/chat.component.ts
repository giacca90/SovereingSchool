import { afterNextRender, ChangeDetectorRef, Component, Input } from '@angular/core';
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
export class ChatComponent {
	@Input() idCurso: number | null = null;
	chat: CursoChat | null = null;

	constructor(
		public chatService: ChatService,
		private route: ActivatedRoute,
		private cdr: ChangeDetectorRef,
	) {
		if (!this.idCurso) {
			this.route.paramMap.subscribe((params) => {
				this.idCurso = params.get('idCurso') as number | null;
			});
		}

		afterNextRender(() => {
			console.log('AFTERNEXTRENDER');
			if (this.idCurso) {
				this.chatService.getChat(this.idCurso).subscribe({
					next: (data: CursoChat | null) => {
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
}
