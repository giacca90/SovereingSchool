import { Component } from '@angular/core';
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
	constructor(
		private loginService: LoginService,
		private chatService: ChatService,
	) {
		if (this.loginService.usuario) {
			this.chatService.initUsuario(this.loginService.usuario.id_usuario);
		}
	}
}
