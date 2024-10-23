import { afterNextRender, ChangeDetectorRef, Component, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import { CursoChat } from '../../../models/CursoChat';
import { InitChatUsuario } from '../../../models/InitChatUsuario';
import { MensajeChat } from '../../../models/MensajeChat';
import { ChatService } from '../../../services/chat.service';
import { LoginService } from '../../../services/login.service';

@Component({
	selector: 'app-home-chat',
	standalone: true,
	imports: [],
	templateUrl: './home-chat.component.html',
	styleUrl: './home-chat.component.css',
})
export class HomeChatComponent {
	chats: MensajeChat[] = [];
	cursos: CursoChat[] = [];
	cargando: boolean = true;

	constructor(
		public router: Router,
		private loginService: LoginService,
		private chatService: ChatService,
		private cdr: ChangeDetectorRef,
		private ngZone: NgZone,
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
			} else {
				console.error('El usuario no esta logueado');
			}
		});
	}

	navegaMensaje(mensaje: MensajeChat) {
		this.ngZone.run(() => {
			this.router.navigate(['chat/', mensaje.id_curso?.toString(), mensaje.id_mensaje]);
		});
	}

	navegaCurso(curso: CursoChat) {
		this.ngZone.run(() => {
			this.router.navigate(['chat/', curso.id_curso.toString()]);
		});
	}
}
