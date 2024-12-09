import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class StreamingService {
	private URL: string = 'https://localhost:8090';
	private webSocketUrlWebcam: string = 'wss://localhost:8090/live-webcam';
	private webSocketUrlOBS: string = 'wss://localhost:8090/live-obs';
	public enGrabacion: boolean = false;
	private ws: WebSocket | null = null;
	private mediaRecorder: MediaRecorder | null = null;
	private rtmpUrl: string | null = null;

	constructor(private http: HttpClient) {}

	getVideo(id_usuario: number, id_curso: number, id_clase: number): Observable<Blob> {
		return this.http.get(`${this.URL}/${id_usuario}/${id_curso}/${id_clase}`, { responseType: 'blob' });
	}

	// TODO: Revisar
	async sendMediaToServer(stream: MediaStream) {
		const status = document.getElementById('status') as HTMLParagraphElement;

		if (!window.WebSocket) {
			console.error('WebSocket no es compatible con este navegador.');
			if (status) {
				status.textContent = 'WebSocket no es compatible con este navegador.';
			}
			return;
		}

		// Abrir conexión WebSocket
		this.ws = new WebSocket(this.webSocketUrlWebcam);
		this.mediaRecorder = new MediaRecorder(stream, {
			mimeType: 'video/webm; codecs=vp9',
		});

		this.mediaRecorder.onstop = () => {
			console.log('Grabación detenida.');
			if (status) {
				status.textContent = 'Grabación detenida.';
			}
		};

		this.mediaRecorder.onerror = (event) => {
			console.error('Error en MediaRecorder:', event);
			if (status) {
				status.textContent = 'Error en MediaRecorder: ' + event;
			}
		};

		this.ws.onopen = () => {
			console.log('Conexión WebSocket establecida.');
			if (status) {
				status.textContent = 'Conexión WebSocket establecida. Enviando flujo de medios...';
			}
			this.enGrabacion = true;

			// Comenzar a grabar y enviar los datos
			if (this.mediaRecorder) {
				this.mediaRecorder.start(200); // Fragmentos de 200 ms
				console.log('Grabación iniciada.');

				this.mediaRecorder.ondataavailable = (event) => {
					if (event.data.size > 0) {
						this.ws?.send(event.data);
					}
				};
			}
		};

		this.ws.onerror = (error) => {
			console.error('Error en WebSocket:', error);
			if (status) {
				status.textContent = 'Error en WebSocket: ' + error;
			}
			this.enGrabacion = false;
		};

		this.ws.onclose = () => {
			console.log('Cerrando MediaRecorder y WebSocket.');
			if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
				this.mediaRecorder.stop();
			}
			if (status) {
				status.textContent = 'Conexión WebSocket cerrada.';
			}

			this.enGrabacion = false;
		};
	}

	// Método para detener la grabación y la conexión
	stopMediaStreaming() {
		const status = document.getElementById('status') as HTMLParagraphElement;

		if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
			this.mediaRecorder.stop(); // Detener la grabación
			console.log('Grabación detenida.');
		} else {
			this.detenerOBS();
		}

		if (this.ws) {
			this.ws.close(); // Cerrar WebSocket
			console.log('WebSocket cerrado.');
		}

		if (status) {
			status.textContent = 'Transmisión detenida.';
		}

		this.enGrabacion = false;
	}

	async startOBS(userId: number) {
		let status: HTMLParagraphElement | null = null;

		if (!window.WebSocket) {
			console.error('WebSocket no es compatible con este navegador.');
			status = document.getElementById('statusOBS') as HTMLParagraphElement;
			if (status) {
				status.textContent = 'WebSocket no es compatible con este navegador.';
			}
			return;
		}

		// Abrir conexión WebSocket
		this.ws = new WebSocket(this.webSocketUrlOBS);

		this.ws.onopen = () => {
			console.log('Conexión WebSocket establecida.');
			status = document.getElementById('statusOBS') as HTMLParagraphElement;
			if (status) {
				status.textContent = 'Conexión WebSocket establecida. Enviando ID del usuario...';

				// Enviar el ID del usuario al servidor
				const message = { type: 'request_rtmp_url', userId: userId.toString() };
				this.ws?.send(JSON.stringify(message));
			} else {
				console.error('status no encontrado');
			}
		};

		this.ws.onmessage = (event) => {
			const data = JSON.parse(event.data);

			if (data.type === 'rtmp_url') {
				console.log('URL RTMP recibida:', data.rtmpUrl);
				this.rtmpUrl = data.rtmpUrl;
				if (status) {
					status.textContent = `URL para OBS recibida: ${data.rtmpUrl}`;
				}

				// Mostrar la URL RTMP al usuario (opcional)
				const obsUrlField = document.getElementById('obs-url') as HTMLInputElement;
				if (obsUrlField) {
					obsUrlField.value = data.rtmpUrl;
				}
			} else if (data.type === 'emitiendoOBS') {
				console.log('Emisión de OBS iniciada.');
				this.enGrabacion = true;
				if (status) {
					status.textContent = 'Emisión de OBS iniciada.';
				}
			} else if (data.type === 'error') {
				console.error('Error recibido del servidor:', data.message);
				this.enGrabacion = false;
				if (status) {
					status.textContent = `Error: ${data.message}`;
				}
			}
		};

		this.ws.onerror = (error) => {
			console.error('Error en la conexión WebSocket:', error);
			this.enGrabacion = false;
			if (status) {
				status.textContent = 'Error en la conexión WebSocket.';
			}
		};

		this.ws.onclose = () => {
			console.log('Conexión WebSocket cerrada.');
			this.enGrabacion = false;
			if (status) {
				status.textContent = 'Conexión WebSocket cerrada.';
			}
		};
	}

	emitirOBS() {
		const status = document.getElementById('statusOBD');
		if (this.ws) {
			this.ws.send(JSON.stringify({ 'event': 'emitirOBS', 'rtmpUrl': this.rtmpUrl }));
			if (status) {
				status.textContent = 'Comenzando la emisión...';
			}
			this.enGrabacion = true;
		} else {
			console.error('No se pudo emitir OBS');
			if (status) {
				status.textContent = 'No se pudo emitir OBS';
			}
		}
	}

	detenerOBS() {
		const status = document.getElementById('statusOBD');
		if (this.ws) {
			this.ws.send(JSON.stringify({ 'event': 'detenerStreamOBS', 'rtmpUrl': this.rtmpUrl }));
			if (status) {
				status.textContent = 'Deteniendo la emisión...';
			}
			this.enGrabacion = true;
		} else {
			console.error('No se pudo detener OBS');
			if (status) {
				status.textContent = 'No se pudo detener OBS';
			}
		}
	}
}
