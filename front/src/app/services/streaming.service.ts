import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { VideoElement } from '../components/editor-curso/editor-webcam/editor-webcam.component';
import { Clase } from '../models/Clase';
import { CursosService } from './cursos.service';
import { LoginService } from './login.service';

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
	public UrlPreview: string = '';
	private streanId: string | null = null;

	constructor(
		private http: HttpClient,
		private cursoService: CursosService,
		private loginService: LoginService,
	) {}

	getVideo(id_usuario: number, id_curso: number, id_clase: number): Observable<Blob> {
		return this.http.get(`${this.URL}/${id_usuario}/${id_curso}/${id_clase}`, { responseType: 'blob' });
	}

	// TODO: Revisar
	/**
	 * Función para emitir un video a través de WebRTC
	 * @param stream MediaStream
	 * @param clase Objeto Clase
	 */
	async emitirWebcam(stream: MediaStream, clase: Clase | null) {
		if (!window.WebSocket) {
			console.error('WebSocket no es compatible con este navegador.');
			return;
		}

		// Obterer lod datos del mediaStream
		const width = stream.getVideoTracks()[0].getSettings().width;
		const height = stream.getVideoTracks()[0].getSettings().height;
		const fps = stream.getVideoTracks()[0].getSettings().frameRate;
		if (!width || !height || !fps) return;

		// Abrir conexión WebSocket
		this.ws = new WebSocket(this.webSocketUrlWebcam);
		this.mediaRecorder = new MediaRecorder(stream, {
			mimeType: 'video/webm; codecs=vp9',
		});

		this.ws.onmessage = (event) => {
			const data = JSON.parse(event.data);
			if (data.type === 'streamId') {
				console.log('ID del stream recibido:', data.streamId);
				this.streanId = data.streamId;
				this.enGrabacion = true;

				// Comenzar a grabar y enviar los datos
				if (this.mediaRecorder) {
					console.log('frame in service: ' + this.mediaRecorder?.stream.getVideoTracks()[0].getSettings().frameRate);
					this.mediaRecorder.start(fps * 0.1); // Fragmentos de 100 ms
					console.log('Grabación iniciada.');

					this.mediaRecorder.ondataavailable = (event) => {
						if (event.data.size > 0) {
							const blob = new Blob([event.data], { type: 'video/webm' });
							this.ws?.send(blob);
						}
					};
				}

				// Actualizar el curso
				if (clase && clase.curso_clase) {
					this.cursoService.getCurso(clase.curso_clase).then((curso) => {
						if (curso) {
							clase.direccion_clase = data.streamId;
							curso.clases_curso?.push(clase);
							this.cursoService.updateCurso(curso).subscribe({
								next: (success: boolean) => {
									if (success) {
										console.log('Curso actualizado con éxito');
									} else {
										console.error('Falló la actualización del curso');
									}
								},
								error: (error) => {
									console.error('Error al actualizar el curso: ' + error);
								},
							});
						}
					});
				}
			}
		};

		this.mediaRecorder.onstop = () => {
			console.log('Grabación detenida.');
		};

		this.mediaRecorder.onerror = (event) => {
			console.error('Error en MediaRecorder:', event);
		};

		this.ws.onopen = () => {
			console.log('Conexión WebSocket establecida.');
			// Enviar el ID del usuario al servidor
			const message = { type: 'userId', userId: this.loginService.usuario?.id_usuario, videoSettings: { width: width, height: height, fps: fps } };
			this.ws?.send(JSON.stringify(message));
		};

		this.ws.onerror = (error) => {
			console.error('Error en WebSocket:', error);
			this.enGrabacion = false;
		};

		this.ws.onclose = () => {
			console.log('Cerrando MediaRecorder y WebSocket.');
			if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
				this.mediaRecorder.stop();
			}
			this.enGrabacion = false;
		};
	}

	// Método para detener la grabación y la conexión
	stopMediaStreaming() {
		const status = document.getElementById('status') as HTMLParagraphElement;
		this.detenerWebcam();

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
					status.textContent = `URL para OBS recibida:`;
				}

				// Mostrar la URL RTMP al usuario
				const enlaces: HTMLDivElement = document.getElementById('enlaces') as HTMLDivElement;
				if (enlaces) {
					const server: HTMLParagraphElement = document.createElement('p') as HTMLParagraphElement;
					server.textContent = 'URL del servidor';
					const lServer: HTMLParagraphElement = document.createElement('p') as HTMLParagraphElement;
					lServer.classList.add('p-2', 'cursor-pointer', 'rounded-lg', 'border', 'border-black', 'text-blue-700');
					lServer.textContent = (data.rtmpUrl as string).substring(0, (data.rtmpUrl as string).lastIndexOf('/'));
					lServer.onclick = () =>
						navigator.clipboard.writeText(lServer.textContent || '').then(() => {
							const tooltip = document.getElementById('tooltip') as HTMLDivElement;
							if (tooltip) {
								tooltip.textContent = 'Copiado al portapapeles';
								setTimeout(() => {
									tooltip.textContent = 'Haz click para copiar';
								}, 3000);
							}
						});
					// Crear tooltip dinámico para lServer
					lServer.addEventListener('mouseover', (event: MouseEvent) => {
						const tooltip = document.createElement('div');
						tooltip.id = 'tooltip';
						tooltip.textContent = 'Haz click para copiar';
						tooltip.classList.add('absolute', 'bg-black', 'text-white', 'text-xs', 'p-1', 'rounded-sm', 'tooltip');
						// Posicionar el tooltip basado en la posición del ratón
						tooltip.style.position = 'fixed'; // Usamos 'fixed' para que funcione con las coordenadas del mouse
						tooltip.style.top = `${event.clientY - 30}px`; // Ajustar posición encima del ratón
						tooltip.style.left = `${event.clientX}px`; // Alinear con el puntero del ratón
						enlaces.appendChild(tooltip);
					});

					lServer.addEventListener('mousemove', (event: MouseEvent) => moveTooltip(event));

					lServer.addEventListener('mouseleave', () => {
						const tooltip = document.getElementById('tooltip') as HTMLDivElement;
						if (tooltip) {
							tooltip.remove();
						}
					});

					const key: HTMLParagraphElement = document.createElement('p') as HTMLParagraphElement;
					key.textContent = 'Clave del stream';
					const lKey: HTMLParagraphElement = document.createElement('p') as HTMLParagraphElement;
					lKey.classList.add('p-2', 'cursor-pointer', 'rounded-lg', 'border', 'border-black', 'text-blue-700');
					lKey.textContent = (data.rtmpUrl as string).substring((data.rtmpUrl as string).lastIndexOf('/') + 1);
					lKey.onclick = () =>
						navigator.clipboard.writeText(lKey.textContent || '').then(() => {
							const tooltip = document.getElementById('tooltip') as HTMLDivElement;
							if (tooltip) {
								tooltip.textContent = 'Copiado al portapapeles';
								setTimeout(() => {
									tooltip.textContent = 'Haz click para copiar';
								}, 3000);
							}
						});
					lKey.addEventListener('mouseover', (event: MouseEvent) => {
						const tooltip = document.createElement('div');
						tooltip.id = 'tooltip';
						tooltip.textContent = 'Haz click para copiar';
						tooltip.classList.add('absolute', 'bg-black', 'text-white', 'text-xs', 'p-1', 'rounded-sm', 'tooltip');
						// Posicionar el tooltip basado en la posición del ratón
						tooltip.style.position = 'fixed'; // Usamos 'fixed' para que funcione con las coordenadas del mouse
						tooltip.style.top = `${event.clientY - 40}px`; // Ajustar posición encima del ratón
						tooltip.style.left = `${event.clientX}px`; // Alinear con el puntero del ratón
						enlaces.appendChild(tooltip);
					});

					lKey.addEventListener('mousemove', (event: MouseEvent) => moveTooltip(event));

					lKey.addEventListener('mouseleave', () => {
						const tooltip = document.getElementById('tooltip') as HTMLDivElement;
						if (tooltip) {
							tooltip.remove();
						}
					});

					// Mover el tooltip con el ratón mientras esté en el elemento
					const moveTooltip = (moveEvent: MouseEvent) => {
						const tooltip = document.getElementById('tooltip') as HTMLDivElement;
						tooltip.style.top = `${moveEvent.clientY - 30}px`;
						tooltip.style.left = `${moveEvent.clientX}px`;
					};
					enlaces.appendChild(server);
					enlaces.appendChild(lServer);
					enlaces.appendChild(key);
					enlaces.appendChild(lKey);
				}

				// Devuelve la URL para la preview
				this.UrlPreview = this.URL + '/getPreview/' + data.rtmpUrl.split('/').pop();
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

	emitirOBS(clase: Clase | null) {
		const status = document.getElementById('statusOBD');
		if (this.ws) {
			this.ws.send(JSON.stringify({ 'event': 'emitirOBS', 'rtmpUrl': this.rtmpUrl }));
			if (status) {
				status.textContent = 'Comenzando la emisión...';
			}
			this.enGrabacion = true;
			if (clase && this.rtmpUrl) {
				clase.direccion_clase = this.rtmpUrl.substring(this.rtmpUrl.lastIndexOf('/') + 1);
				if (clase?.curso_clase) {
					this.cursoService.getCurso(clase?.curso_clase).then((curso) => {
						if (curso) {
							curso.clases_curso?.push(clase);
							this.cursoService.updateCurso(curso).subscribe({
								next: (success: boolean) => {
									if (success) {
										console.log('Curso actualizado con éxito');
									} else {
										console.error('Falló la actualización del curso');
									}
								},
								error: (error) => {
									console.error('Error al actualizar el curso: ' + error);
								},
							});
						}
					});
				}
			}
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

	detenerWebcam() {
		const status = document.getElementById('status') as HTMLParagraphElement;
		if (this.ws) {
			this.ws.send(JSON.stringify({ 'event': 'detenerStreamWebcam', 'streamId': this.streanId }));
			if (status) {
				status.textContent = 'Deteniendo la emisión...';
			}
			this.enGrabacion = true;
		} else {
			console.error('No se pudo detener WebRTC');
			if (status) {
				status.textContent = 'No se pudo detener WebRTC';
			}
		}
	}

	closeConnections() {
		if (this.ws?.OPEN) {
			this.ws.close();
		}
	}

	getPresets() {
		return this.http.get(`${this.URL}/presets/get/${this.loginService.usuario?.id_usuario}`, { responseType: 'json' });
	}

	savePresets(presets: Map<string, { elements: VideoElement[]; shortcut: string }>) {
		const presetsObj = Object.fromEntries(presets);
		console.log('Presets:', presets);
		console.log('Presets JSON:', JSON.stringify(presetsObj));
		console.log('URL:', `${this.URL}/presets/save/${this.loginService.usuario?.id_usuario}`);
		this.http.put(`${this.URL}/presets/save/${this.loginService.usuario?.id_usuario}`, JSON.stringify(presetsObj)).subscribe((response) => {
			console.log('Respuesta:', response);
		});
	}
}
