import { afterNextRender, ChangeDetectorRef, Component } from '@angular/core';
import { CursoChat } from '../../models/CursoChat';
import { InitChatUsuario } from '../../models/InitChatUsuario';
import { MensajeChat } from '../../models/MensajeChat';
import { ChatService } from '../../services/chat.service';
import { LoginService } from '../../services/login.service';

@Component({
	selector: 'app-chat',
	standalone: true,
	imports: [],
	templateUrl: './chat.component.html',
	styleUrl: './chat.component.css',
})
export class ChatComponent {
	chats: MensajeChat[] = [];
	cursos: CursoChat[] = [];
	cargando: boolean = true;

	constructor(
		private loginService: LoginService,
		private chatService: ChatService,
		private cdr: ChangeDetectorRef,
	) {
		afterNextRender(() => {
			if (this.loginService.usuario) {
				this.chatService.initUsuario(this.loginService.usuario.id_usuario).subscribe({
					next: (init: InitChatUsuario | null) => {
						console.log('LLEGA LA RESPUESTA AL COMPONENTE: ', init);
						if (init && init.mensajes && init.cursos && init.idUsuario === this.loginService.usuario?.id_usuario) {
							this.chats = init.mensajes;
							this.cursos = init.cursos;
							this.cargando = false;
							this.cdr.detectChanges();
						} else {
							this.cargando = false;
							this.cdr.detectChanges();
						}
					},
					error: (e: Error) => {
						console.error('Error en recibir la respuesta: ' + e.message);
					},
				});
			}
		});
	}
}
