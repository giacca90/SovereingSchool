import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class ChatService {
	private url: string = 'ws://localhost:8080/chat-socket';
	private socket: WebSocket = new WebSocket(this.url);

	initUsuario(idUsuario: number): Observable<InitChatUsuario> {
		this.socket.onopen = () => {
			console.log('Conexión establecida');
			// Puedes enviar un mensaje inicial aquí
			const message = {
				type: 'init_chat',
				user_id: idUsuario,
			};
			this.socket.send(JSON.stringify(message));
		};

		this.socket.onmessage = (event: MessageEvent) => {
			const message = JSON.parse(event.data);
			console.log('Mensaje recibido:', message);
			// Aquí puedes actualizar tu componente con los datos recibidos
			return message;
		};

		this.socket.onerror = (error) => {
			console.error('Error en la conexión:', error);
		};

		this.socket.onclose = (event) => {
			console.log('Conexión cerrada:', event);
			// Puedes intentar reconectar aquí
		};
	}
}
