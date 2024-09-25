import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject, takeUntil } from 'rxjs';
import { CursoChat } from '../models/CursoChat';
import { InitChatUsuario } from '../models/InitChatUsuario';

@Injectable({
	providedIn: 'root',
})
export class ChatService {
	private url: string = 'ws://localhost:8070/chat-socket';
	private initSubject = new BehaviorSubject<InitChatUsuario | null>(null); // Utiliza BehaviorSubject para emitir el último valor a nuevos suscriptores
	private cursoSubject = new BehaviorSubject<CursoChat | null>(null);
	private unsubscribe$ = new Subject<void>();
	private client: Client;

	constructor() {
		this.client = new Client({
			brokerURL: this.url,
		});

		this.client.onWebSocketError = (error) => {
			console.error('Error con WebSocket', error);
		};

		this.client.onStompError = (frame) => {
			console.error('Broker reported error: ' + frame.headers['message']);
			console.error('Additional details: ' + frame.body);
		};
	}

	initUsuario(idUsuario: number): Observable<InitChatUsuario | null> {
		console.log('INITUSUARIO');

		this.client.onConnect = (frame) => {
			console.log('Connected: ' + frame);

			// Suscríbete a las respuestas del backend
			this.client.subscribe('/init_chat/result', (response) => {
				console.log('RESPONSE: ', response.body);
				const init: InitChatUsuario = JSON.parse(response.body) as InitChatUsuario;
				console.log('SE RECIBE RESPUESTA DEL BACK!!!', init);
				console.log('INIT.MENSAJES', init.mensajes);

				// Emitir el valor recibido a través del subject
				this.initSubject.next(init);
			});

			// Publicar el mensaje al backend
			this.client.publish({
				destination: '/app/init',
				body: idUsuario.toString(),
			});
		};

		// Activa el WebSocket
		this.client.activate();

		// Devolver el observable que los componentes pueden suscribirse
		return this.initSubject.asObservable().pipe(
			takeUntil(this.unsubscribe$), // Desuscribirse cuando sea necesario
		);
	}

	getChat(idCurso: number): Observable<CursoChat | null> {
		// Suscríbete a las respuestas del backend
		this.client.subscribe('/init_chat/' + idCurso, (response) => {
			console.log('RESPONSE: ', response.body);
			const curso: CursoChat = JSON.parse(response.body) as CursoChat;
			console.log('SE RECIBE RESPUESTA DEL BACK!!!', curso);

			// Emitir el valor recibido
			this.cursoSubject.next(curso);
		});

		// Publicar el mensaje al backend
		this.client.publish({
			destination: '/app/curso',
			body: idCurso.toString(),
		});

		// Devolver el observable que los componentes pueden suscribirse
		return this.cursoSubject.asObservable().pipe(
			takeUntil(this.unsubscribe$), // Desuscribirse cuando sea necesario
		);
	}
}
