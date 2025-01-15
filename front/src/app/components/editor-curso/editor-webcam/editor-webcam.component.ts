import { Component, ElementRef, OnInit, QueryList, ViewChildren } from '@angular/core';

@Component({
	selector: 'app-editor-webcam',
	standalone: true,
	imports: [],
	templateUrl: './editor-webcam.component.html',
	styleUrls: ['./editor-webcam.component.css'],
})
export class EditorWebcamComponent implements OnInit {
	devices: MediaDeviceInfo[] = []; // Lista de dispositivos
	capturas: MediaStream[] = []; // Lista de capturas

	// Variables para manejar la posición, escala y estado del video
	private dragging = false; // Indica si un video está siendo arrastrado
	private mouseX = 0; // Posición X del ratón sobre el canvas
	private mouseY = 0; // Posición Y del ratón sobre el canvas
	private videoScale = 1; // Escala inicial del video
	private currentVideoElement: HTMLVideoElement | null = null; // Video actual que se está arrastrando
	dragVideo: HTMLVideoElement | null = null; // Video que se está arrastrando
	dragPosition = { x: 0, y: 0 }; // Posición del ratón mientras se arrastra
	@ViewChildren('videoElement') videoElements!: QueryList<ElementRef<HTMLVideoElement>>;

	async ngOnInit() {
		try {
			// Solicitar permisos para cámara y micrófono
			await navigator.mediaDevices.getUserMedia({ video: true, audio: true });

			// Listar todos los dispositivos multimedia
			this.devices = await navigator.mediaDevices.enumerateDevices();

			// Iniciar la configuración de medios
			this.startMedias();

			// Escuchar cambios en los dispositivos multimedia
			navigator.mediaDevices.ondevicechange = async () => {
				console.log('Cambio detectado en los dispositivos');
				await this.updateDevices();
			};
		} catch (error) {
			console.error('Error al acceder a los dispositivos:', error);
		}
	}

	startMedias() {
		// Asignar el video stream a cada dispositivo de video
		this.devices.forEach((device) => {
			console.log('Device: ' + device.label);
			if (device.kind === 'videoinput') {
				this.getVideoStream(device.deviceId);
			} else if (device.kind === 'audioinput') {
				this.getAudioStream(device.deviceId);
			}
		});
	}

	async updateDevices() {
		try {
			// Enumerar nuevamente los dispositivos disponibles
			const allDevices = await navigator.mediaDevices.enumerateDevices();

			// Identificar dispositivos nuevos y agregarlos
			allDevices.forEach((device) => {
				const exists = this.devices.some((d) => d.deviceId === device.deviceId);
				if (!exists) {
					console.log('Dispositivo nuevo detectado:', device.label || 'Sin nombre');

					// Agregar el dispositivo a la lista
					this.devices.push(device);

					// Configurar el flujo para el nuevo dispositivo
					if (device.kind === 'videoinput') {
						this.getVideoStream(device.deviceId);
					} else if (device.kind === 'audioinput') {
						this.getAudioStream(device.deviceId);
					}
				}
			});

			// Identificar dispositivos desconectados y eliminarlos
			const disconnectedDevices = this.devices.filter((device) => !allDevices.some((d) => d.deviceId === device.deviceId));

			if (disconnectedDevices.length > 0) {
				console.log('Dispositivos desconectados:', disconnectedDevices);
				// Limpiar recursos de dispositivos desconectados
				disconnectedDevices.forEach((device) => {
					console.log('Dispositivo desconectado:', device.label || 'Sin nombre');

					// Detener flujos activos asociados al dispositivo
					if (device.kind === 'videoinput' || device.kind === 'audioinput') {
						const element = document.getElementById(device.deviceId) as HTMLVideoElement | HTMLAudioElement;
						if (element && element.srcObject) {
							const stream = element.srcObject as MediaStream;
							stream.getTracks().forEach((track) => track.stop()); // Detener cada pista del flujo
							element.srcObject = null; // Limpiar la referencia al flujo
						}
					}
				});
			}

			console.log('Dispositivos actualizados:', this.devices);
		} catch (error) {
			console.error('Error al actualizar dispositivos:', error);
		}
	}

	async getVideoStream(deviceId: string): Promise<void> {
		try {
			const stream = await navigator.mediaDevices.getUserMedia({
				video: { deviceId: { exact: deviceId } },
			});

			// Encontrar el elemento <video> con el mismo ID que el dispositivo
			const videoElement = this.videoElements.find((el) => el.nativeElement.id === deviceId);
			if (videoElement) {
				videoElement.nativeElement.srcObject = stream; // Asignar el stream al video
			}
		} catch (error) {
			console.error('Error al obtener el stream de video:', error);
		}
	}

	async getAudioStream(deviceId: string): Promise<void> {
		try {
			const stream = await navigator.mediaDevices.getUserMedia({
				audio: { deviceId: { exact: deviceId } },
			});

			const audioLevelElement = document.getElementById(deviceId) as HTMLDivElement;
			if (audioLevelElement) {
				this.visualizeAudio(stream, audioLevelElement); // Iniciar visualización de audio
			}
		} catch (error) {
			console.error('Error al obtener el stream de audio:', error);
		}
	}

	visualizeAudio(stream: MediaStream, audioLevel: HTMLDivElement): void {
		const audioContext = new AudioContext();
		const analyser = audioContext.createAnalyser();
		const source = audioContext.createMediaStreamSource(stream);

		source.connect(analyser);
		analyser.fftSize = 256;

		const dataArray = new Uint8Array(analyser.frequencyBinCount);

		function updateAudioLevel() {
			analyser.getByteFrequencyData(dataArray);
			const volume = Math.max(...dataArray) / 255;
			const percentage = Math.min(volume * 100, 100);
			audioLevel.style.width = `${percentage}%`; // Ajustar el ancho de la barra
			requestAnimationFrame(updateAudioLevel);
		}

		updateAudioLevel();
	}

	async addScrean() {
		console.log('Añadir pantalla');
		try {
			// Solicitar al usuario que seleccione una ventana, aplicación o pantalla
			const stream: MediaStream = await navigator.mediaDevices.getDisplayMedia({
				video: true,
				audio: true, // Opcional: captura el audio del sistema si es compatible
			});
			this.capturas.push(stream);

			// Crear un elemento `<video>` dinámico para mostrar el contenido capturado
			const videoElement = document.createElement('video');
			videoElement.autoplay = true;
			videoElement.playsInline = true;
			videoElement.srcObject = stream;

			// Añadir el elemento al DOM para mostrar el stream
			document.getElementById('capturas')?.appendChild(videoElement);
			videoElement.classList.add('m-2', 'rounded-lg', 'border', 'border-black', 'w-1/6');

			// Manejar el fin de la captura
			stream.getVideoTracks()[0].onended = () => {
				console.log('La captura ha terminado');
				this.capturas = this.capturas.filter((s) => s !== stream);
			};
		} catch (error) {
			console.error('Error al capturar ventana o pantalla:', error);
		}
	}

	dragStart(event: DragEvent, deviceId: string): void {
		event.dataTransfer?.setData('deviceId', deviceId);
		const videoElement = document.getElementById(deviceId) as HTMLVideoElement;

		if (videoElement) {
			this.dragVideo = videoElement; // Guarda el video en el estado
		} else {
			console.log('No hay videoElement');
		}

		// Elimina el efecto visual del ghosting
		const img = new Image(); // Crea una imagen vacía
		img.src = ''; // No asignamos ninguna fuente
		event.dataTransfer?.setDragImage(img, 0, 0); // Aplica la imagen vacía
	}

	dragOver(event: DragEvent) {
		event.preventDefault();
		if (!this.dragVideo) return; // Solo continua si hay un video en arrastre

		const canvas = document.getElementById('salida') as HTMLCanvasElement;
		const rect = canvas.getBoundingClientRect();

		// Relación de escala entre el tamaño visual y el tamaño interno del canvas
		const scaleX = canvas.width / rect.width;
		const scaleY = canvas.height / rect.height;

		// Ajustar la posición del ratón al tamaño interno del canvas
		this.dragPosition.x = Math.round((event.clientX - rect.left) * scaleX);
		this.dragPosition.y = Math.round((event.clientY - rect.top) * scaleY);

		// Dibuja una cruz y el marco del video mientras se arrastra
		const context = canvas.getContext('2d');
		if (context) {
			context.clearRect(0, 0, canvas.width, canvas.height); // Limpiar el canvas

			// Dibujar cruz en el centro del ratón, extendida hasta los bordes del canvas
			context.strokeStyle = 'blue';
			context.lineWidth = 3;
			context.beginPath();

			// Línea horizontal (extendida hasta los bordes)
			context.moveTo(0, this.dragPosition.y); // Comienza desde el borde izquierdo
			context.lineTo(canvas.width, this.dragPosition.y); // Termina en el borde derecho

			// Línea vertical (extendida hasta los bordes)
			context.moveTo(this.dragPosition.x, 0); // Comienza desde el borde superior
			context.lineTo(this.dragPosition.x, canvas.height); // Termina en el borde inferior

			// Dibuja la cruz
			context.stroke();

			// Dibuja el video en su escala actual
			const videoWidth = (this.dragVideo.videoWidth * this.videoScale) / 5;
			const videoHeight = (this.dragVideo.videoHeight * this.videoScale) / 5;
			const drawX = this.dragPosition.x - videoWidth / 2;
			const drawY = this.dragPosition.y - videoHeight / 2;

			context.drawImage(this.dragVideo, drawX, drawY, videoWidth, videoHeight);

			// Dibuja un marco azul alrededor del video
			context.strokeStyle = 'blue'; // Color azul para el marco
			context.lineWidth = 3; // Grosor del marco
			context.strokeRect(drawX, drawY, videoWidth, videoHeight); // Dibuja el rectángulo
		} else {
			console.log('No hay contexto');
		}
	}

	drop(event: DragEvent): void {
		event.preventDefault();
		if (!this.dragVideo) return; // Solo continua si hay un video en arrastre

		const paintedVideo: HTMLVideoElement = this.dragVideo;
		const canvas = document.getElementById('salida') as HTMLCanvasElement;
		const context = canvas.getContext('2d');
		if (context) {
			// Inicia el bucle para mantener el video en movimiento en el canvas
			const videoWidth = (paintedVideo.videoWidth * this.videoScale) / 5;
			const videoHeight = (paintedVideo.videoHeight * this.videoScale) / 5;
			const drawX = this.dragPosition.x - videoWidth / 2;
			const drawY = this.dragPosition.y - videoHeight / 2;

			const drawFrame = () => {
				context.clearRect(0, 0, canvas.width, canvas.height);
				context.drawImage(paintedVideo!, drawX, drawY, videoWidth, videoHeight);
				requestAnimationFrame(drawFrame);
			};

			drawFrame(); // Comienza el bucle de renderizado
			this.dragVideo = null; // Limpia el estado del video arrastrado
		}
	}

	drawCross(context: CanvasRenderingContext2D, x: number, y: number): void {
		context.clearRect(0, 0, context.canvas.width, context.canvas.height); // Limpiar el canvas

		// Dibujar la cruz
		context.beginPath();
		context.strokeStyle = 'red';
		context.lineWidth = 2;

		context.moveTo(x - 10, y);
		context.lineTo(x + 10, y);
		context.moveTo(x, y - 10);
		context.lineTo(x, y + 10);

		context.stroke();
	}

	drawVideo(context: CanvasRenderingContext2D, video: HTMLVideoElement, x: number, y: number): void {
		const canvasWidth = context.canvas.width;
		const canvasHeight = context.canvas.height;

		const videoWidth = (video.videoWidth * this.videoScale) / 5; // Tamaño proporcional
		const videoHeight = (video.videoHeight * this.videoScale) / 5;

		// Limitar el video al tamaño del canvas
		const drawX = Math.max(0, Math.min(x - videoWidth / 2, canvasWidth - videoWidth));
		const drawY = Math.max(0, Math.min(y - videoHeight / 2, canvasHeight - videoHeight));

		// Dibujar el video dentro del canvas
		context.clearRect(0, 0, canvasWidth, canvasHeight); // Limpiar el canvas
		context.drawImage(video, drawX, drawY, videoWidth, videoHeight);
	}

	wheel(event: WheelEvent): void {
		console.log('Evento Scroll: ', event.deltaY);
		if (!this.dragVideo) return; // Solo ajusta si hay un video en arrastre

		event.preventDefault();
		this.videoScale += event.deltaY < 0 ? 0.05 : -0.05; // Cambia el tamaño
		this.videoScale = Math.max(0.1, this.videoScale); // Escala mínima
		console.log('Escala: ', this.videoScale);
	}
}
