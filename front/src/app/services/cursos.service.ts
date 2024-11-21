import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, firstValueFrom, map, Observable, of } from 'rxjs';
import { Clase } from '../models/Clase';
import { Curso } from '../models/Curso';
import { Usuario } from '../models/Usuario';

@Injectable({
	providedIn: 'root',
})
export class CursosService {
	backURL: string = 'https://localhost:8080';
	backURLStreaming: string = 'https://localhost:8090';
	webSocketUrl: string = 'wss://localhost:8080/live-webcam';
	public cursos: Curso[] = [];

	constructor(private http: HttpClient) {}

	async getCurso(id_curso: number) {
		for (let i = 0; i < this.cursos.length; i++) {
			if (this.cursos[i].id_curso == id_curso) {
				if (this.cursos[i].clases_curso === undefined) {
					try {
						const response = await firstValueFrom(this.http.get<Curso>(`${this.backURL}/cursos/getCurso/${id_curso}`));
						this.cursos[i].clases_curso = response.clases_curso?.sort((a, b) => a.posicion_clase - b.posicion_clase);
						this.cursos[i].descriccion_larga = response.descriccion_larga;
						this.cursos[i].fecha_publicacion_curso = response.fecha_publicacion_curso;
						this.cursos[i].planes_curso = response.planes_curso;
						this.cursos[i].precio_curso = response.precio_curso;
						return this.cursos[i];
					} catch (error) {
						console.error('Error en cargar curso:', error);
						return null;
					}
				} else return this.cursos[i];
			}
		}
		return null;
	}

	updateCurso(curso: Curso | null): Observable<boolean> {
		if (curso === null) {
			console.error('El curso no existe!!!');
			return of(false);
		}
		return this.http.put<string>(`${this.backURL}/cursos/update`, curso, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response) => {
				if (response.ok) {
					return true;
				} else {
					console.error('Respuesta del back: ' + response.body);
					return false;
				}
			}),
			catchError((e: Error) => {
				console.error('Error en actualizar el curso: ' + e.message);
				return of(false);
			}),
		);
	}

	deleteClass(clase: Clase): Observable<boolean> {
		const curso_clase: number | undefined = clase.curso_clase;
		clase.curso_clase = undefined;
		return this.http.delete<string>(this.backURL + '/cursos/' + curso_clase + '/deleteClase/' + clase.id_clase, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.ok) {
					this.cursos[this.cursos.findIndex((curso) => curso.id_curso === curso_clase)].clases_curso = undefined;
					return true;
				}
				return false;
			}),
			catchError((e: Error) => {
				console.error('Error en crear la clase: ' + e.message);
				return of(false);
			}),
		);
	}

	subeVideo(file: File, idCurso: number, idClase: number): Observable<string | null> {
		const formData = new FormData();
		formData.append('video', file, file.name);

		return this.http.post<string>(this.backURL + '/cursos/subeVideo/' + idCurso + '/' + idClase, formData, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.ok) {
					return response.body;
				}
				return null;
			}),
			catchError((e: Error) => {
				console.error('Error en subir el video: ' + e.message);
				return of(null);
			}),
		);
	}

	addImagenCurso(target: FormData): Observable<string | null> {
		return this.http.post<string[]>(this.backURL + '/usuario/subeFotos', target, { observe: 'response' }).pipe(
			map((response: HttpResponse<string[]>) => {
				if (response.ok && response.body) {
					return response.body[0];
				}
				return null;
			}),
			catchError((e: Error) => {
				console.error('Error en subir la imagen: ' + e.message);
				return of(null);
			}),
		);
	}

	getCursosProfe(profe: Usuario) {
		const cursosProfe: Curso[] = [];
		this.cursos.forEach((curso) => {
			curso.profesores_curso.forEach((profe2) => {
				if (profe2.id_usuario === profe.id_usuario) {
					cursosProfe.push(curso);
				}
			});
		});
		return cursosProfe;
	}

	deleteCurso(curso: Curso): Observable<boolean> {
		return this.http.delete<string>(this.backURL + '/cursos/delete/' + curso.id_curso, { observe: 'response', responseType: 'text' as 'json' }).pipe(
			map((response: HttpResponse<string>) => {
				if (response.ok) {
					this.cursos = this.cursos.slice(
						this.cursos.findIndex((curso2) => curso2.id_curso === curso.id_curso),
						1,
					);
					return true;
				}
				return false;
			}),
			catchError((e: Error) => {
				console.error('Error en eliminar el curso: ' + e.message);
				return of(false);
			}),
		);
	}

	getStatusCurso(id_usuario: number, id_curso: number): Observable<number | boolean> {
		return this.http.get<number>(this.backURLStreaming + '/status/' + id_usuario + '/' + id_curso, { observe: 'response' }).pipe(
			map((response: HttpResponse<number>) => {
				if (response.ok && response.body) {
					return response.body;
				}
				return false;
			}),
		);
	}

	// TODO: Revisar
	async sendMediaToServer() {
		const status = document.getElementById('status') as HTMLParagraphElement;

		if (!window.WebSocket) {
			console.error('WebSocket no es compatible con este navegador.');
			if (status) {
				status.textContent = 'WebSocket no es compatible con este navegador.';
			}
			return;
		}

		// Abrir conexión WebSocket
		const ws = new WebSocket(this.webSocketUrl);

		ws.onopen = () => {
			console.log('Conexión WebSocket establecida.');
			if (status) {
				status.textContent = 'Conexión WebSocket establecida. Enviando flujo de medios...';
			}
		};

		ws.onerror = (error) => {
			console.error('Error en WebSocket:', error);
			if (status) {
				status.textContent = 'Error en WebSocket: ' + error;
			}
		};

		ws.onclose = () => {
			console.log('Conexión WebSocket cerrada.');
			if (status) {
				status.textContent = 'Conexión WebSocket cerrada.';
			}
		};

		try {
			// Obtener el flujo de la webcam y el micrófono
			const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
			const videoTracks = stream.getVideoTracks();
			const audioTracks = stream.getAudioTracks();

			if (videoTracks.length === 0) {
				console.error('No se encontraron pistas de video.');
				if (status) {
					status.textContent = 'No se encontraron pistas de video.';
				}
				return;
			}

			if (audioTracks.length === 0) {
				console.error('No se encontraron pistas de audio.');
				if (status) {
					status.textContent = 'No se encontraron pistas de audio.';
				}
				return;
			}

			const mediaRecorder = new MediaRecorder(stream, {
				mimeType: 'video/webm; codecs=vp9',
			});

			mediaRecorder.ondataavailable = (event) => {
				if (event.data.size > 0) {
					ws.send(event.data);
				}
			};

			mediaRecorder.onstop = () => {
				console.log('Grabación detenida.');
				if (status) {
					status.textContent = 'Grabación detenida.';
				}
			};

			mediaRecorder.onerror = (event) => {
				console.error('Error en MediaRecorder:', event);
				if (status) {
					status.textContent = 'Error en MediaRecorder: ' + event;
				}
			};

			// Comenzar a grabar y enviar los datos
			mediaRecorder.start(500); // Fragmentos de 500 ms
			console.log('Grabación iniciada.');

			// Detener la grabación cuando el WebSocket se cierra
			ws.onclose = () => {
				console.log('Cerrando MediaRecorder.');
				mediaRecorder.stop();
			};
		} catch (error) {
			console.error('Error al capturar medios:', error);
			if (status) {
				status.textContent = 'Error al capturar medios: ' + error;
			}
		}
	}
}
