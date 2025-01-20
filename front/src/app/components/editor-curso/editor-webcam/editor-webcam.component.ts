import { AfterViewInit, Component, ElementRef, OnInit, QueryList, ViewChildren } from '@angular/core';

@Component({
	selector: 'app-editor-webcam',
	standalone: true,
	imports: [],
	templateUrl: './editor-webcam.component.html',
	styleUrls: ['./editor-webcam.component.css'],
})
export class EditorWebcamComponent implements OnInit, AfterViewInit {
	videoDevices: MediaDeviceInfo[] = []; // Lista de dispositivos de video
	audioDevices: MediaDeviceInfo[] = []; // Lista de dispositivos de audio
	capturas: MediaStream[] = []; // Lista de capturas
	videosElements: VideoElement[] = []; // Lista de elementos de video
	dragVideo: VideoElement | null = null; // Video que se está arrastrando
	dragPosition = { x: 0, y: 0 }; // Posición del ratón mientras se arrastra
	canvas: HTMLCanvasElement | null = null;
	context: CanvasRenderingContext2D | null = null;
	@ViewChildren('videoElement') videoElements!: QueryList<ElementRef<HTMLVideoElement>>;

	async ngOnInit() {
		try {
			// Solicitar permisos para cámara y micrófono
			await navigator.mediaDevices.getUserMedia({ video: true, audio: true });

			// Listar todos los dispositivos multimedia
			const devices = await navigator.mediaDevices.enumerateDevices();

			// Iniciar la configuración de medios
			this.startMedias(devices);

			// Escuchar cambios en los dispositivos multimedia
			navigator.mediaDevices.ondevicechange = async () => {
				console.log('Cambio detectado en los dispositivos');
				await this.updateDevices();
			};
		} catch (error) {
			console.error('Error al acceder a los dispositivos:', error);
		}
	}

	ngAfterViewInit(): void {
		this.canvas = document.getElementById('salida') as HTMLCanvasElement;
		this.context = this.canvas.getContext('2d');

		const drawFrame = () => {
			if (!this.canvas || !this.context) return;
			this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
			this.videosElements.forEach((elemento) => {
				if (!elemento.painted || !elemento.position || !this.context) return;
				const videoWidth = elemento.element.videoWidth * elemento.scale;
				const videoHeight = elemento.element.videoHeight * elemento.scale;
				this.context.drawImage(elemento.element!, elemento.position.x, elemento.position.y, videoWidth, videoHeight);
			});
			requestAnimationFrame(drawFrame);
		};

		drawFrame(); // Comienza el bucle de renderizado
	}

	startMedias(devices: MediaDeviceInfo[]) {
		// Asignar el video stream a cada dispositivo de video
		devices.forEach((device) => {
			console.log('Device: ' + device.label);
			if (device.kind === 'videoinput') {
				this.videoDevices.push(device);
				this.getVideoStream(device.deviceId);
			} else if (device.kind === 'audioinput') {
				this.audioDevices.push(device);
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
				if (device.kind === 'videoinput') {
					const exists = this.videoDevices.some((d) => d.deviceId === device.deviceId);
					if (!exists) {
						console.log('Dispositivo nuevo detectado:', device.label || 'Sin nombre');
						// Agregar el dispositivo a la lista
						this.videoDevices.push(device);
						this.getVideoStream(device.deviceId);
					}
				}

				if (device.kind === 'audioinput') {
					const exists = this.audioDevices.some((d) => d.deviceId === device.deviceId);
					if (!exists) {
						console.log('Dispositivo nuevo detectado:', device.label || 'Sin nombre');
						// Agregar el dispositivo a la lista
						this.audioDevices.push(device);
						this.getAudioStream(device.deviceId);
					}
				}
			});

			// Identificar dispositivos de video desconectados y eliminarlos
			const disconnectedVideoDevices = this.videoDevices.filter((device) => !allDevices.some((d) => d.deviceId === device.deviceId));

			if (disconnectedVideoDevices.length > 0) {
				console.log('Dispositivos desconectados:', disconnectedVideoDevices);
				// Limpiar recursos de dispositivos desconectados
				disconnectedVideoDevices.forEach((device) => {
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

			// Identificar dispositivos de audio desconectados y eliminarlos
			const disconnectedDevices = this.audioDevices.filter((device) => !allDevices.some((d) => d.deviceId === device.deviceId));

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

			console.log('Dispositivos actualizados:', this.videoDevices + '\n' + this.audioDevices);
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
				const ele: VideoElement = {
					id: deviceId,
					element: videoElement.nativeElement,
					painted: false,
					scale: 1,
					position: null,
				};
				this.videosElements.push(ele);
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

			const ele: VideoElement = {
				id: stream.id,
				element: videoElement,
				painted: false,
				scale: 1,
				position: null,
			};
			this.videosElements.push(ele);
			// Manejar el fin de la captura
			stream.getVideoTracks()[0].onended = () => {
				console.log('La captura ha terminado');
				this.capturas = this.capturas.filter((s) => s !== stream);
			};
		} catch (error) {
			console.error('Error al capturar ventana o pantalla:', error);
		}
	}

	mousedown(event: MouseEvent, deviceId: string): void {
		console.log('Mousedown');
		const videoElement = document.getElementById(deviceId) as HTMLVideoElement;
		if (!videoElement) {
			console.error('No hay videoElement');
			return;
		}
		const ele = this.videosElements.find((el) => el.id === deviceId);
		if (!ele || !this.canvas) {
			console.error('No hay elemento');
			return;
		}

		this.dragVideo = ele;

		const ghost = videoElement.cloneNode(true) as HTMLVideoElement;
		ghost.classList.add('ghost-video'); // Clase para estilizar el ghost
		ghost.classList.remove('rounded-lg');
		ghost.style.position = 'absolute';
		ghost.style.pointerEvents = 'none'; // Para que no interfiera con eventos
		ghost.style.zIndex = '1000';

		// Ajustar dimensiones del ghost para que coincidan con el video original
		ghost.style.width = `${videoElement.offsetWidth}px`;
		ghost.style.height = `${videoElement.offsetHeight}px`;

		// Copiar estilos clave
		ghost.style.transform = getComputedStyle(videoElement).transform || 'none';
		ghost.style.objectFit = getComputedStyle(videoElement).objectFit || 'contain';
		ghost.style.fontFamily = getComputedStyle(videoElement).fontFamily || 'inherit';

		// Copiar fuente y poner en marcha
		ghost.srcObject = ele.element.srcObject;
		ghost.load();

		// Dimensiones del video

		const updateGhostPosition = (x: number, y: number, element: HTMLVideoElement) => {
			const elementWidth = element.offsetWidth;
			const elementHeight = element.offsetHeight;
			const offsetX = elementWidth / 2;
			const offsetY = elementHeight / 2;

			ghost.style.left = `${x - offsetX}px`;
			ghost.style.top = `${y - offsetY}px`;
		};

		updateGhostPosition(event.clientX, event.clientY, videoElement); // Posición inicial
		document.body.appendChild(ghost);

		// Evento para detectar `wheel`
		const wheel = (wheelEvent: WheelEvent) => {
			console.log('Wheel');
			if (!this.dragVideo || !this.canvas) return;

			const rect = this.canvas.getBoundingClientRect();
			const isMouseOverCanvas: boolean = wheelEvent.clientX >= rect.left && wheelEvent.clientX <= rect.right && wheelEvent.clientY >= rect.top && wheelEvent.clientY <= rect.bottom;

			if (isMouseOverCanvas) {
				wheelEvent.preventDefault();
				// Obtener tamaño actual del ghost
				const ghostStyles = window.getComputedStyle(ghost);
				const currentWidth = parseFloat(ghostStyles.width);
				const currentHeight = parseFloat(ghostStyles.height);

				// Obtener posición actual del ghost
				const currentLeft = parseFloat(ghostStyles.left);
				const currentTop = parseFloat(ghostStyles.top);

				// Incrementar o reducir tamaño en función del scroll
				const delta = wheelEvent.deltaY < 0 ? 1.05 : 0.95; // Aumenta o reduce en un 5%
				const newWidth = currentWidth * delta;
				const newHeight = currentHeight * delta;

				// Calcular la diferencia de tamaño para ajustar la posición
				const widthDiff = newWidth - currentWidth;
				const heightDiff = newHeight - currentHeight;

				// Ajustar la posición del ghost para mantener el centro alineado con el ratón
				ghost.style.left = `${currentLeft - widthDiff / 2}px`;
				ghost.style.top = `${currentTop - heightDiff / 2}px`;

				// Actualizar dimensiones del ghost
				ghost.style.width = `${Math.max(10, newWidth)}px`; // Asegurarse de que no sea demasiado pequeño
				ghost.style.height = `${Math.max(10, newHeight)}px`;
			}
		};

		document.addEventListener('wheel', wheel, { passive: false });

		// Evento para mover el ghost
		const mousemove = (moveEvent: MouseEvent) => {
			console.log('Mousemove');
			try {
				if (!this.dragVideo || !this.canvas) return;

				updateGhostPosition(moveEvent.clientX, moveEvent.clientY, ghost);

				const rect = this.canvas.getBoundingClientRect();
				const ghostRect = ghost.getBoundingClientRect();

				// Calcular las coordenadas de intersección
				const intersection = {
					left: Math.max(rect.left, ghostRect.left),
					top: Math.max(rect.top, ghostRect.top),
					right: Math.min(rect.right, ghostRect.right),
					bottom: Math.min(rect.bottom, ghostRect.bottom),
				};

				// Verificar si hay intersección
				const isIntersecting = intersection.left < intersection.right && intersection.top < intersection.bottom;

				// Verificar si el ghost está completamente contenido dentro del canvas
				const isFullyContained = ghostRect.left >= rect.left && ghostRect.top >= rect.top && ghostRect.right <= rect.right && ghostRect.bottom <= rect.bottom;

				if (isIntersecting) {
					console.log('Intersección encontrada');
					ghost.style.clipPath = `polygon(
			        ${((intersection.left - ghostRect.left) / ghostRect.width) * 100}% 
			        ${((intersection.top - ghostRect.top) / ghostRect.height) * 100}%, 
			        ${((intersection.right - ghostRect.left) / ghostRect.width) * 100}% 
			        ${((intersection.top - ghostRect.top) / ghostRect.height) * 100}%, 
			        ${((intersection.right - ghostRect.left) / ghostRect.width) * 100}% 
			        ${((intersection.bottom - ghostRect.top) / ghostRect.height) * 100}%, 
			        ${((intersection.left - ghostRect.left) / ghostRect.width) * 100}% 
			        ${((intersection.bottom - ghostRect.top) / ghostRect.height) * 100}%
					)`;
					ghost.classList.remove('ghost-video');

					// Detectar posición del cursor respecto al canvas

					if (isFullyContained) {
						ghost.classList.remove('border-red-700');
						ghost.classList.add('border-blue-700', 'border-2');
						this.canvas.classList.remove('border-red-700');
						this.canvas.classList.add('border-blue-700', 'border-2');
					} else {
						ghost.classList.remove('border-blue-700');
						ghost.classList.add('border-red-700', 'border-2');
						this.canvas.classList.remove('border-blue-700');
						this.canvas.classList.add('border-red-700', 'border-2');
					}
				} else {
					console.log('No hay intersección');
					ghost.style.clipPath = 'none'; // Restaurar si no hay intersección
					ghost.classList.add('ghost-video');
					ghost.classList.remove('border-blue-700', 'border-2');
					this.canvas.classList.remove('border-blue-700', 'border-2');
				}

				const isMouseOverCanvas: boolean = moveEvent.clientX >= rect.left && moveEvent.clientX <= rect.right && moveEvent.clientY >= rect.top && moveEvent.clientY <= rect.bottom;
				console.log('Mouse over canvas: ', isMouseOverCanvas);
				const orizontal = document.getElementById('orizontal') as HTMLDivElement;
				if (!orizontal.classList.contains('hedden')) {
					orizontal.classList.add('hedden');
				}
				orizontal.style.display = 'none';
				const vertical = document.getElementById('vertical') as HTMLDivElement;
				if (!vertical.classList.contains('hedden')) {
					vertical.classList.add('hedden');
				}
				vertical.style.display = 'none';
				if (isIntersecting && orizontal && vertical) {
					// Mostrar la cruz
					const cursorPosition = {
						isAbove: moveEvent.clientY < rect.top,
						isBelow: moveEvent.clientY > rect.bottom,
						isLeft: moveEvent.clientX < rect.left,
						isRight: moveEvent.clientX > rect.right,
					};
					if ((cursorPosition.isLeft || cursorPosition.isRight) && !cursorPosition.isAbove && !cursorPosition.isBelow) {
						orizontal.classList.remove('hedden');
						orizontal.style.display = 'block';
					}
					if ((cursorPosition.isAbove || cursorPosition.isBelow) && !cursorPosition.isLeft && !cursorPosition.isRight) {
						vertical.classList.remove('hedden');
						vertical.style.display = 'block';
					}
					/* if ((cursorPosition.isAbove || cursorPosition.isBelow) && !cursorPosition.isLeft && !cursorPosition.isRight) {
						vertical.classList.remove('hedden');
						vertical.style.display = 'block';
					}
					if ((cursorPosition.isLeft || cursorPosition.isRight) && !cursorPosition.isAbove && !cursorPosition.isBelow) {
						orizontal.classList.remove('hedden');
						orizontal.style.display = 'block';
					} */

					if (isMouseOverCanvas) {
						vertical.classList.remove('hedden');
						vertical.style.display = 'block';
						orizontal.classList.remove('hedden');
						orizontal.style.display = 'block';
					}

					if (isFullyContained) {
						vertical.classList.remove('bg-red-700');
						vertical.classList.add('bg-blue-700');

						orizontal.classList.remove('bg-red-700');
						orizontal.classList.add('bg-blue-700');
					} else {
						vertical.classList.remove('bg-blue-700');
						vertical.classList.add('bg-red-700');

						orizontal.classList.remove('bg-blue-700');
						orizontal.classList.add('bg-red-700');
					}
					vertical.style.left = `${moveEvent.clientX - rect.left}px`;
					orizontal.style.top = `${moveEvent.clientY - rect.top}px`;
				}
			} catch (error) {
				console.error('Error al mover el video: ', error);
			}
		};
		document.addEventListener('mousemove', mousemove);

		// Evento para soltar el ratón
		const mouseup = (upEvent: MouseEvent) => {
			console.log('MOUSEUP');
			if (!this.dragVideo || !this.canvas) return;

			const rect = this.canvas.getBoundingClientRect();
			const isMouseOverCanvas: boolean = upEvent.clientX >= rect.left && upEvent.clientX <= rect.right && upEvent.clientY >= rect.top && upEvent.clientY <= rect.bottom;

			if (isMouseOverCanvas) {
				// Relación de escala entre el tamaño visual y el interno del canvas
				const scaleX = this.canvas.width / rect.width;
				const scaleY = this.canvas.height / rect.height;

				// Dimensiones del ghost en el documento
				const ghostRect = ghost.getBoundingClientRect();
				const ghostWidthInCanvas = ghostRect.width * scaleX; // Ajustado al canvas
				const ghostHeightInCanvas = ghostRect.height * scaleY;

				// Dimensiones originales del video
				const originalWidth = this.dragVideo.element.videoWidth;
				const originalHeight = this.dragVideo.element.videoHeight;

				// Calculamos la escala requerida
				const requiredScaleX = ghostWidthInCanvas / originalWidth;
				const requiredScaleY = ghostHeightInCanvas / originalHeight;
				const requiredScale = Math.min(requiredScaleX, requiredScaleY);

				// Dimensiones escaladas
				const scaledWidth = originalWidth * requiredScale;
				const scaledHeight = originalHeight * requiredScale;

				// Ajustamos la posición para centrar el ratón en el video escalado
				const canvasX = (upEvent.clientX - rect.left) * scaleX - scaledWidth / 2;
				const canvasY = (upEvent.clientY - rect.top) * scaleY - scaledHeight / 2;

				// Guardar datos en el objeto VideoElement
				this.dragVideo.position = { x: canvasX, y: canvasY };
				this.dragVideo.scale = requiredScale; // Escala que garantiza el tamaño correcto en el canvas
				this.dragVideo.painted = true; // Marcamos el video como "pintado"

				const cross = document.getElementById('cross') as HTMLDivElement;
				if (cross) {
					cross.style.display = 'none';
					cross.classList.add('hidden');
				}
			}

			// Restaurar estado
			this.canvas.classList.remove('border-blue-700', 'border-2');
			this.dragVideo = null;
			ghost.remove();
			document.removeEventListener('mousemove', mousemove);
			document.removeEventListener('wheel', wheel);
			document.removeEventListener('mouseup', mouseup);
		};

		document.addEventListener('mouseup', mouseup);
	}

	dragStart(event: DragEvent, deviceId: string): void {
		event.dataTransfer?.setData('deviceId', deviceId);
		const videoElement = document.getElementById(deviceId) as HTMLVideoElement;

		if (videoElement) {
			const ele = this.videosElements.find((el) => el.id === deviceId);
			if (ele) {
				this.dragVideo = ele; // Guarda el video en el estado
			}
		} else {
			console.log('No hay videoElement');
		}

		// Crear el elemento "ghost"
		const ghost = videoElement.cloneNode(true) as HTMLVideoElement;
		ghost.classList.add('ghost-video'); // Agregar clase CSS para estilizar el ghost
		ghost.style.position = 'absolute';
		ghost.style.pointerEvents = 'none'; // Para que no interfiera con otros eventos
		ghost.style.zIndex = '1000';
		document.body.appendChild(ghost);
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
			const videoWidth = (this.dragVideo.element.videoWidth * this.dragVideo.scale) / 5;
			const videoHeight = (this.dragVideo.element.videoHeight * this.dragVideo.scale) / 5;
			const drawX = this.dragPosition.x - videoWidth / 2;
			const drawY = this.dragPosition.y - videoHeight / 2;

			context.drawImage(this.dragVideo.element, drawX, drawY, videoWidth, videoHeight);

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
		this.dragVideo.painted = true;
		const canvas = document.getElementById('salida') as HTMLCanvasElement;
		const context = canvas.getContext('2d');
		if (context) {
			// Inicia el bucle para mantener el video en movimiento en el canvas
			const videoWidth = (this.dragVideo.element.videoWidth * this.dragVideo.scale) / 5;
			const videoHeight = (this.dragVideo.element.videoHeight * this.dragVideo.scale) / 5;
			const drawX = this.dragPosition.x - videoWidth / 2;
			const drawY = this.dragPosition.y - videoHeight / 2;
			this.dragVideo.position = { x: drawX, y: drawY };
			this.dragVideo = null; // Limpia el estado del video arrastrado
		}
	}

	wheel(event: WheelEvent): void {
		console.log('Evento Scroll: ', event.deltaY);
		if (!this.dragVideo) return; // Solo ajusta si hay un video en arrastre

		event.preventDefault();
		this.dragVideo.scale += event.deltaY < 0 ? 0.05 : -0.05; // Cambia el tamaño
		this.dragVideo.scale = Math.max(0.1, this.dragVideo.scale); // Escala mínima
		console.log('Escala: ', this.dragVideo.scale);
	}
}

export interface VideoElement {
	id: string;
	element: HTMLVideoElement;
	painted: boolean;
	scale: number;
	position: { x: number; y: number } | null;
}
