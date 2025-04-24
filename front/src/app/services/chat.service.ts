import { isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject, switchMap, takeUntil, timer } from 'rxjs';
import SockJS from 'sockjs-client';
import { CursoChat } from '../models/CursoChat';
import { InitChatUsuario } from '../models/InitChatUsuario';
import { MensajeChat } from '../models/MensajeChat';
import { LoginService } from './login.service';

@Injectable({
	providedIn: 'root',
})
export class ChatService {
	private url: string = 'wss://localhost:8070/chat-socket';
	public initSubject = new BehaviorSubject<InitChatUsuario | null>(null);
	private cursoSubject = new BehaviorSubject<CursoChat | null>(null);
	private unsubscribe$ = new Subject<void>();
	private jwtToken: string | null = localStorage.getItem('Token');
	private client: Client = new Client({
		webSocketFactory: () => new SockJS('https://localhost:8070/chat-socket'),
		reconnectDelay: 1000,
		connectHeaders: {
			Authorization: 'Bearer ' + this.jwtToken,
		},
		onWebSocketError: (error: Error) => console.error('Error con WebSocket', error.message),
		onStompError: (frame: { headers: { [key: string]: string }; body: string }) => {
			console.error('Broker reported error: ' + frame.headers['message']);
			console.error('Additional details: ' + frame.body);
		},
	});
	private currentSubscription: StompSubscription | null = null; // Guardar referencia a la suscripción actual

	constructor(
		private loginService: LoginService,
		@Inject(PLATFORM_ID) private platformId: object, // Inject PLATFORM_ID to detect server or browser
	) {
		if (isPlatformBrowser(this.platformId)) {
			this.client.onWebSocketError = (error) => {
				console.error('Error con Websocket', error.message);
			};

			this.client.onStompError = (frame) => {
				console.error('Broker reported error: ' + frame.headers['message']);
				console.error('Additional details: ' + frame.body);
			};

			this.client.onConnect = (frame) => {
				console.log('Connected: ' + frame);
				if (this.loginService.usuario) {
					this.initUsuario(this.loginService.usuario.id_usuario);
				}
			};

			// Activa el WebSocke
			this.client.activate();
		}
	}

	private initUsuario(idUsuario: number): Observable<InitChatUsuario | null> {
		console.log('INITUSUARIO');
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

		// Devolver el observable que los componentes pueden suscribirse
		return this.initSubject.asObservable();
	}

	getChat(idCurso: number): Observable<CursoChat | null> {
		// Si no está conectado, esperar 500ms antes de continuar
		if (!this.client.connected) {
			return timer(500).pipe(
				// Retrasa la ejecución 500ms
				switchMap(() => this.getChat(idCurso)), // Llama a getChat nuevamente después del retraso
			);
		}
		// Si hay una suscripción anterior, desuscríbete antes de suscribirte a la nueva
		if (this.currentSubscription) {
			this.currentSubscription.unsubscribe();
			this.cursoSubject.next(null);
		}

		// Suscríbete a las respuestas del backend
		this.currentSubscription = this.client.subscribe('/init_chat/' + idCurso, (response) => {
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

	enviarMensaje(idCurso: number | null, clase: number, value: string, respuesta: string | null, pregunta: { minute: number; second: number } | null) {
		let resp: MensajeChat | null = null;
		let preg: number | null = null;
		if (pregunta) {
			preg = pregunta.minute * 60 + pregunta.second;
		}
		if (respuesta) {
			resp = new MensajeChat(
				respuesta, // id mensaje
				idCurso, // id curso
				clase, // id clase
				this.loginService.usuario?.id_usuario, // id usuario
				null, // nombre curso
				null, // nombre clase
				null, // nombre usuario
				null, // foto curso
				null, // foto usuario
				null, // respuesta
				preg, // pregunta
				null, // mensaje
				null, // fecha
			);
		}

		const mensaje: MensajeChat = new MensajeChat(
			null, // id mensaje
			idCurso, // id curso
			clase, // id clase
			this.loginService.usuario?.id_usuario, // id usuario
			null, // nombre curso
			null, // nombre clase
			null, // nombre usuario
			null, // foto curso
			null, // foto usuario
			resp, // respuesta
			preg, // pregunta
			value, // mensaje
			new Date(), // fecha
		);

		// Publicar el mensaje al backend
		this.client.publish({
			destination: '/app/chat',
			body: JSON.stringify(mensaje),
		});
	}

	mensajeLeido(idMensaje: string) {
		this.client.publish({
			destination: '/app/leido',
			body: JSON.stringify(this.loginService.usuario?.id_usuario + ',' + idMensaje),
		});
	}
}
