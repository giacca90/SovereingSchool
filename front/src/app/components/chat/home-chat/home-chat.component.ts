import { afterNextRender, ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { CursoChat } from '../../../models/CursoChat';
import { InitChatUsuario } from '../../../models/InitChatUsuario';
import { MensajeChat } from '../../../models/MensajeChat';
import { Usuario } from '../../../models/Usuario';
import { ChatService } from '../../../services/chat.service';
import { LoginService } from '../../../services/login.service';

@Component({
	selector: 'app-home-chat',
	standalone: true,
	imports: [],
	templateUrl: './home-chat.component.html',
	styleUrl: './home-chat.component.css',
})
export class HomeChatComponent implements OnDestroy {
	chats: MensajeChat[] = [];
	cursos: CursoChat[] = [];
	cargando: boolean = true;
	private subscription: Subscription = new Subscription();

	constructor(
		private loginService: LoginService,
		private chatService: ChatService,
		private cdr: ChangeDetectorRef,
		private usuario: Usuario | null = null,
	) {
		this.subscription.add(
			this.loginService.usuario$.subscribe((usuario) => {
				this.usuario = usuario;
			}),
		);

		afterNextRender(() => {
			if (this.usuario) {
				this.chatService.initUsuario(this.usuario.id_usuario).subscribe({
					next: (init: InitChatUsuario | null) => {
						console.log('LLEGA LA RESPUESTA AL COMPONENTE: ', init);
						if (init && init.mensajes && init.cursos && init.idUsuario === this.usuario?.id_usuario) {
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
	ngOnDestroy(): void {
		this.subscription.unsubscribe();
	}
}
