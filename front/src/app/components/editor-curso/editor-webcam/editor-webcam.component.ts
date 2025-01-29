import { AfterViewInit, Component, ElementRef, HostListener, OnInit, QueryList, ViewChildren } from '@angular/core';

@Component({
	selector: 'app-editor-webcam',
	standalone: true,
	imports: [],
	templateUrl: './editor-webcam.component.html',
	styleUrls: ['./editor-webcam.component.css'],
})
export class EditorWebcamComponent implements OnInit, AfterViewInit {
	canvasWidth = 1280;
	canvasHeight = 720;
	canvasFPS = 30;
	isResolutionSelectorVisible = false;
	videoDevices: MediaDeviceInfo[] = []; // Lista de dispositivos de video
	audioDevices: MediaDeviceInfo[] = []; // Lista de dispositivos de audio
	capturas: MediaStream[] = []; // Lista de capturas
	staticContent: File[] = []; // Lista de archivos estáticos
	videosElements: VideoElement[] = []; // Lista de elementos de video
	dragVideo: VideoElement | null = null; // Video que se está arrastrando
	dragPosition = { x: 0, y: 0 }; // Posición del ratón mientras se arrastra
	canvas: HTMLCanvasElement | null = null;
	context: CanvasRenderingContext2D | null = null;
	editandoDimensiones = false; // Indica si se está editando las dimensiones de un video
	presets = new Map<string, { elements: VideoElement[]; shortcut: string }>(); // Presets
	private fileUrlCache = new Map<File, string>(); // Cache de URLs de archivos
	@ViewChildren('videoElement') videoElements!: QueryList<ElementRef<HTMLVideoElement>>;

	@HostListener('window:resize', ['$event'])
	onResize(): void {
		this.calculatePreset();
	}

	async ngOnInit() {
		try {
			// Solicitar permisos para cámara y micrófono
			const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });

			// Listar todos los dispositivos multimedia
			const devices = await navigator.mediaDevices.enumerateDevices();
			stream.getTracks().forEach((track) => {
				track.stop();
			});

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
		let frameInterval; // Tiempo entre frames en milisegundos

		const drawFrame = () => {
			if (!this.canvas || !this.context) return;
			frameInterval = 1000 / this.canvasFPS;
			this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
			this.videosElements.forEach((elemento) => {
				if (!elemento.painted || !elemento.position || !this.context) return;
				if (elemento.element instanceof HTMLVideoElement) {
					const videoWidth = elemento.element.videoWidth * elemento.scale;
					const videoHeight = elemento.element.videoHeight * elemento.scale;
					this.context.drawImage(elemento.element!, elemento.position.x, elemento.position.y, videoWidth, videoHeight);
				} else if (elemento.element instanceof HTMLImageElement) {
					const imageWidth = elemento.element.naturalWidth * elemento.scale;
					const imageHeight = elemento.element.naturalHeight * elemento.scale;
					this.context.drawImage(elemento.element, elemento.position.x, elemento.position.y, imageWidth, imageHeight);
				}
			});
		};

		setInterval(drawFrame, frameInterval); // Comienza el bucle de renderizado
	}

	startMedias(devices: MediaDeviceInfo[]) {
		// Asignar el video stream a cada dispositivo de video
		devices.forEach((device) => {
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
			let stream = await navigator.mediaDevices.getUserMedia({
				video: { deviceId: { exact: deviceId } },
			});

			// Obtener datos del dispositivo
			let videoTrack = stream.getVideoTracks()[0];
			const capabilities = videoTrack.getCapabilities(); // Capacidades del dispositivo
			let settings = videoTrack.getSettings(); // Configuración actual

			console.log('Capabilities:', capabilities.width?.max, 'x', capabilities.height?.max);
			console.log('Current Settings:', settings.width, 'x', settings.height);

			stream.getTracks().forEach((track) => {
				track.stop();
			});

			// Seleccionar valores específicos dentro de las capacidades
			const constraints: MediaStreamConstraints = {
				video: {
					deviceId: { ideal: deviceId },
					width: { exact: capabilities.width?.max }, // Máximo permitido
					height: { exact: capabilities.height?.max }, // Máximo permitido
					frameRate: { exact: capabilities.frameRate?.max }, // Máximo permitido
				},
			};

			stream = await navigator.mediaDevices.getUserMedia(constraints);
			videoTrack = stream.getVideoTracks()[0];
			settings = videoTrack.getSettings();
			console.log('New Current Settings:', settings.width, 'x', settings.height);

			// Encontrar el elemento <video> con el mismo ID que el dispositivo
			const div = document.getElementById('div-' + deviceId);
			if (!div) return;
			const resolution = div.querySelector('#resolution');
			if (!resolution) return;
			resolution.innerHTML = `${settings.width}x${settings.height} ${settings.frameRate}fps`;

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

			const audioTrack = stream.getAudioTracks()[0];
			const settings = audioTrack.getSettings();

			console.log('Audio Settings:', settings);

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
		try {
			// Solicitar al usuario que seleccione una ventana, aplicación o pantalla
			const stream: MediaStream = await navigator.mediaDevices.getDisplayMedia({
				video: true,
				audio: true, // Opcional: captura el audio del sistema si es compatible
			});
			this.capturas.push(stream);
			setTimeout(() => {
				const div = document.getElementById('div-' + stream.id);
				if (!div) return;
				const resolution = div.querySelector('#resolution');
				if (!resolution) return;
				const settings = stream.getVideoTracks()[0].getSettings();
				resolution.innerHTML = `${settings.width}x${settings.height} ${settings.frameRate}fps`;

				const videoElement = this.videoElements.find((el) => el.nativeElement.id === stream.id);
				if (videoElement) {
					videoElement.nativeElement.srcObject = stream;
					const ele: VideoElement = {
						id: stream.id,
						element: videoElement.nativeElement,
						painted: false,
						scale: 1,
						position: null,
					};
					this.videosElements.push(ele);
				}
			}, 100);

			// Manejar el fin de la captura
			stream.getVideoTracks()[0].onended = () => {
				console.log('La captura ha terminado');
				this.capturas = this.capturas.filter((s) => s !== stream);
				// Eliminar el objeto ele del array videosElements
				this.videosElements = this.videosElements.filter((v) => v.id !== stream.id);
			};
		} catch (error) {
			console.error('Error al capturar ventana o pantalla:', error);
		}
	}

	addFiles() {
		const input: HTMLInputElement = document.createElement('input');
		input.type = 'file';
		input.accept = 'image/* video/* audio/*';
		input.multiple = true;
		input.onchange = (event: Event) => {
			const target = event.target as HTMLInputElement;
			if (!target.files || target.files.length === 0) {
				console.error('No se seleccionaron archivos');
				return;
			}
			// Convertir FileList a un array para trabajar con los archivos
			this.staticContent = this.staticContent.concat(Array.from(target.files));
			// espera una decima de segundo que se renderizen en el front
			setTimeout(() => {
				this.staticContent.forEach((file) => {
					const div = document.getElementById('div-' + file.name);
					if (div) {
						if (file.type.startsWith('image/')) {
							const img = document.getElementById(file.name) as HTMLImageElement;
							if (img) {
								const elemento: VideoElement = {
									id: file.name,
									element: img,
									painted: false,
									scale: 1,
									position: null,
								};
								this.videosElements.push(elemento);
							}
						} else if (file.type.startsWith('video/')) {
							const video = document.getElementById(file.name) as HTMLVideoElement;
							if (video) {
								const elemento: VideoElement = {
									id: file.name,
									element: video,
									painted: false,
									scale: 1,
									position: null,
								};
								this.videosElements.push(elemento);
							}
						}
					}
				});
			}, 100);
		};
		input.click();
	}

	getFileUrl(file: File): string {
		if (!this.fileUrlCache.has(file)) {
			const url = URL.createObjectURL(file);
			this.fileUrlCache.set(file, url);
		}
		return this.fileUrlCache.get(file) as string;
	}

	cambiarResolucion($event: Event, res: string) {
		const selected = document.getElementById('selected') as HTMLDivElement;
		const value = selected.querySelector('#value');
		if (!selected || !value) return;
		const string = ($event.target as HTMLDivElement).innerHTML;
		const [width, height] = res.split('x');
		this.canvasWidth = parseInt(width);
		this.canvasHeight = parseInt(height);
		value.innerHTML = string;
		this.isResolutionSelectorVisible = false;
	}

	cambiarFPS(fps: string) {
		this.canvasFPS = parseInt(fps);
	}

	// Empieza el arrastre de un elemento
	mousedown(event: MouseEvent, deviceId: string): void {
		event.preventDefault();
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

		let ghost: HTMLVideoElement | HTMLImageElement;
		if (this.dragVideo.element instanceof HTMLVideoElement) {
			ghost = videoElement.cloneNode(true) as HTMLVideoElement;
		} else if (this.dragVideo.element instanceof HTMLImageElement) {
			ghost = videoElement.cloneNode(true) as HTMLImageElement;
		} else {
			console.error('Tipo de elemento no reconocido');
			return;
		}
		document.body.classList.add('cursor-grabbing');
		ghost.classList.remove('rounded-lg');
		ghost.style.position = 'absolute';
		ghost.style.pointerEvents = 'none';
		ghost.style.zIndex = '1000';

		// Ajustar dimensiones del ghost para que coincidan con el video original
		ghost.style.width = `${videoElement.offsetWidth}px`;
		ghost.style.height = `${videoElement.offsetHeight}px`;

		// Copiar fuente y poner en marcha si es un video
		if (ele.element instanceof HTMLVideoElement && ghost instanceof HTMLVideoElement) {
			ghost.srcObject = ele.element.srcObject;
			ghost.load();
		} else if (ele.element instanceof HTMLImageElement && ghost instanceof HTMLImageElement) {
			ghost.src = ele.element.src;
		} else {
			console.error('Tipo de elemento no reconocido');
			return;
		}

		// Dimensiones del video

		const updateGhostPosition = (x: number, y: number, element: HTMLElement) => {
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
			if (!this.dragVideo) {
				console.error('No hay video arrastrando');
				return;
			}
			if (!this.canvas) {
				console.error('No hay canvas');
				return;
			}

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
		const canvasContainer = document.getElementById('canvas-container') as HTMLDivElement;
		const cross = document.getElementById('cross') as HTMLDivElement;
		if (!canvasContainer || !cross) {
			console.error('No se pudo crear el elemento de la cruz');
			return;
		}
		cross.style.display = 'block';

		const mousemove = (moveEvent: MouseEvent) => {
			try {
				if (!this.dragVideo || !this.canvas) {
					console.error('No hay video arrastrando o canvas');
					return;
				}

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

					if (isFullyContained) {
						ghost.style.border = '2px solid #1d4ed8';
						this.canvas.style.border = '2px solid #1d4ed8';
					} else {
						ghost.style.border = '2px solid #b91c1c';
						this.canvas.style.border = '2px solid #b91c1c';
					}
				} else {
					ghost.style.clipPath = 'none'; // Restaurar si no hay intersección
					ghost.style.border = '1px solid black';
					this.canvas.style.border = '1px solid black';
				}

				const intersecciones = this.colisiones(ghost);

				if (isIntersecting) {
					// Mostrar la cruz
					this.moverCruzPosicionamiento(moveEvent.clientX, moveEvent.clientY, intersecciones);

					if (intersecciones.length > 0) {
						ghost.style.border = '2px solid #b91c1c';
					} else {
						ghost.style.border = '2px solid #1d4ed8';
					}
					intersecciones?.forEach((elemento) => {
						if (elemento.id === 'canvas-container') {
							if (this.canvas) {
								this.canvas.style.border = '2px solid #b91c1c';
							}
						} else {
							elemento.style.border = '2px solid #b91c1c';
							elemento.style.visibility = 'visible';
						}
					});
				}
			} catch (error) {
				console.error('Error al mover el video: ', error);
			}
		};
		document.addEventListener('mousemove', mousemove);

		// Evento para soltar el ratón
		const mouseup = (upEvent: MouseEvent) => {
			if (!this.dragVideo || !this.canvas) {
				console.error('No hay video arrastrando o canvas');
				return;
			}

			const rect = this.canvas.getBoundingClientRect();
			const isMouseOverCanvas: boolean = upEvent.clientX >= rect.left && upEvent.clientX <= rect.right && upEvent.clientY >= rect.top && upEvent.clientY <= rect.bottom;

			if (isMouseOverCanvas) {
				const ghostRect = ghost.getBoundingClientRect();
				const result: VideoElement | undefined = this.paintInCanvas(ghost, ghostRect.width, ghostRect.height, upEvent.clientX, upEvent.clientY);
				// Guardar datos en el objeto VideoElement
				if (!result) return;
				this.dragVideo.scale = result.scale;
				this.dragVideo.position = result.position;
				this.dragVideo.painted = result.painted;

				// Añade una capa encima al elemento transmitido
				this.addCapa(this.dragVideo);
				// Quita la cruz de posicionamiento
				const cross = document.getElementById('cross') as HTMLDivElement;
				if (cross) {
					cross.style.display = 'none';
				}
			}

			// Restaurar estado
			this.canvas.style.border = '1px solid black';
			this.dragVideo = null;
			//ghost.style.visibility = 'hidden';
			ghost.remove();
			cross.style.display = 'none';
			document.removeEventListener('mousemove', mousemove);
			document.removeEventListener('wheel', wheel);
			document.removeEventListener('mouseup', mouseup);
			document.body.classList.remove('cursor-grabbing');
		};
		document.addEventListener('mouseup', mouseup);
	}

	canvasMouseMove(event: MouseEvent) {
		event.preventDefault();
		const canvasContainer = document.getElementById('canvas-container') as HTMLDivElement;
		if (!this.canvas || !canvasContainer || this.editandoDimensiones) return;

		const rect = this.canvas.getBoundingClientRect();
		// Obtener las coordenadas relativas al tamaño visible del canvas
		const mousex = Math.max(0, Math.round(event.clientX - rect.left)); // Redondear y evitar valores negativos
		const mousey = Math.max(0, Math.round(event.clientY - rect.top)); // Redondear y evitar valores negativos

		// Relación de escala entre el tamaño interno del canvas y el tamaño visible
		const scaleX = this.canvas.width / rect.width; // Relación horizontal
		const scaleY = this.canvas.height / rect.height; // Relación vertical

		// Obtener las coordenadas internas (relativas al tamaño interno del canvas)
		const internalMouseX = mousex * scaleX;
		const internalMouseY = mousey * scaleY;

		// Obtener las coordenadas de cada video renderizado
		const rendered = this.videosElements.filter((video) => video.painted);
		const originalGhost = document.getElementById('marco') as HTMLDivElement;
		rendered.forEach((video) => {
			let videoWidth: number = 0;
			let videoHeight: number = 0;
			if (video.element instanceof HTMLVideoElement) {
				videoWidth = video.element.videoWidth * video.scale;
				videoHeight = video.element.videoHeight * video.scale;
			} else if (video.element instanceof HTMLImageElement) {
				videoWidth = video.element.naturalWidth * video.scale;
				videoHeight = video.element.naturalHeight * video.scale;
			} else {
				console.error('Tipo de elemento no reconocido');
			}
			// Coordenadas del video en el canvas
			const videoLeft = video.position ? video.position.x : 0;
			const videoTop = video.position ? video.position.y : 0;

			// Comprobar si el ratón está dentro del área del video
			const isMouseOverVideo = internalMouseX >= videoLeft && internalMouseX <= videoLeft + videoWidth && internalMouseY >= videoTop && internalMouseY <= videoTop + videoHeight;

			// Buscar el elemento "ghost"
			let ghostDiv = canvasContainer.querySelector(`#marco-${CSS.escape(video.id)}`) as HTMLDivElement;
			if (!ghostDiv) {
				ghostDiv = originalGhost.cloneNode(true) as HTMLDivElement;
				const tiradores = ghostDiv.querySelectorAll('[id*="tirador-"]') as NodeListOf<HTMLDivElement>; // Seleccionar los tiradores
				tiradores.forEach((tirador: HTMLDivElement) => {
					tirador.addEventListener('mousedown', (event: MouseEvent) => {
						this.redimensionado(event); // Llamar a la función original
					});
				});

				if (!ghostDiv) return;
				ghostDiv.id = 'marco-' + video.id;
				canvasContainer.appendChild(ghostDiv);
			}

			if (isMouseOverVideo) {
				// Calcular la posición y tamaño del "ghost" en el espacio visible del canvas
				const ghostLeft = videoLeft / scaleX; // Convertir a coordenadas internas del canvas
				const ghostTop = videoTop / scaleY; // Convertir a coordenadas internas del canvas
				const ghostWidth = videoWidth / scaleX; // Ajustar el tamaño para la visualización
				const ghostHeight = videoHeight / scaleY; // Ajustar el tamaño para la visualización

				// Crear o actualizar el elemento del "ghost"
				ghostDiv.style.position = 'absolute'; // Asegurarse de que el ghostDiv se posicione correctamente
				ghostDiv.style.left = `${ghostLeft}px`; // Colocar el "ghost" en la posición correcta
				ghostDiv.style.top = `${ghostTop}px`; // Colocar el "ghost" en la posición correcta
				ghostDiv.style.width = `${ghostWidth}px`; // Ajustar el ancho del "ghost"
				ghostDiv.style.height = `${ghostHeight}px`; // Ajustar la altura del "ghost"
				ghostDiv.style.visibility = 'visible'; // Hacerlo visible

				const buttonX = ghostDiv.querySelector('#buttonx') as HTMLButtonElement;
				buttonX.onclick = () => {
					video.painted = false;
					video.position = null;
					video.scale = 1;
					ghostDiv.remove();
					const capa = document.getElementById('capa-' + video.id);
					if (capa) {
						capa.remove();
					}
					const marco = document.getElementById('marco-' + video.id);
					if (marco) {
						marco.remove();
					}
				};

				// Calcular la longitud de la línea diagonal (de esquina superior izquierda a inferior derecha)
				const diagonalLength = Math.sqrt(Math.pow(ghostDiv.clientWidth, 2) + Math.pow(ghostDiv.clientHeight, 2));

				const line1 = ghostDiv.querySelector('#line1') as HTMLDivElement;
				line1.style.width = `${diagonalLength}px`;
				line1.style.transform = `rotate(${Math.atan2(ghostDiv.clientHeight, ghostDiv.clientWidth)}rad)`;

				const line2 = ghostDiv.querySelector('#line2') as HTMLDivElement;
				// Aplicar estilos dinámicos
				line2.style.width = `${diagonalLength}px`;
				line2.style.transform = `rotate(${-Math.atan2(ghostDiv.clientHeight, ghostDiv.clientWidth)}rad)`;

				// Colocar la línea en la esquina superior derecha
				line2.style.right = '0px';
				line2.style.top = '0px';
			} else {
				// Eliminar el elemento del "ghost" si ya no está sobre el video
				ghostDiv.style.visibility = 'hidden';
			}
		});
	}

	canvasMouseLeave() {
		const rendered = this.videosElements.filter((video) => video.painted);
		if (rendered.length > 0) {
			rendered.forEach((video) => {
				const marco = document.getElementById('marco-' + video.id);
				if (marco) {
					marco.style.visibility = 'hidden';
				}
			});
		}
	}

	redimensionado($event: MouseEvent) {
		const canvasContainer = document.getElementById('canvas-container') as HTMLDivElement;
		const tiradorId = ($event.target as HTMLElement).id; // ID del tirador
		const ghostId = ($event.target as HTMLElement).parentElement?.id; // ID del padre
		const posicionInicial = { x: $event.clientX, y: $event.clientY };
		if (!tiradorId || !ghostId || !canvasContainer || !this.canvas) return;
		const ghostDiv = document.getElementById(ghostId);
		if (!ghostDiv) return;

		this.editandoDimensiones = true;
		// Añadir la cruz de posicionamiento
		const cross = document.getElementById('cross') as HTMLDivElement;
		if (!cross) return;
		cross.style.display = 'block';
		let intersecciones = this.colisiones(ghostDiv);

		// Calcular el centro del ghost para posicionar las lineas
		const rect = this.canvas.getBoundingClientRect();
		const centroX = ghostDiv.offsetLeft + ghostDiv.offsetWidth / 2 + rect.x;
		const centroY = ghostDiv.offsetTop + ghostDiv.offsetHeight / 2 + rect.y;
		this.moverCruzPosicionamiento(centroX, centroY, intersecciones);
		intersecciones?.forEach((elemento) => {
			if (elemento.id === 'canvas-container') {
				if (this.canvas) {
					this.canvas.style.border = '2px solid #b91c1c';
				}
			} else {
				elemento.style.border = '2px solid #b91c1c';
				elemento.style.visibility = 'visible';
			}
		});

		// En evento mousemove, se calcula la diferencia de posición entre el momento del click y el movimiento
		const mouseMove = ($event2: MouseEvent) => {
			const difX = $event2.clientX - posicionInicial.x;
			const difY = $event2.clientY - posicionInicial.y;
			if (!this.canvas) return;
			this.canvas.style.border = '2px solid #1d4ed8';
			const elementos = canvasContainer.querySelectorAll('[id^="marco"]') as NodeListOf<HTMLDivElement>;
			elementos.forEach((elemento) => {
				if (elemento.id !== ghostDiv.id) {
					elemento.style.visibility = 'hidden';
				}
			});

			// Función para recalcular las líneas diagonales
			const recalculaDiagonales = () => {
				const linea1: HTMLDivElement | null = ghostDiv.querySelector('#line1');
				const linea2: HTMLDivElement | null = ghostDiv.querySelector('#line2');

				if (!linea1 || !linea2) return;

				// Calcular la longitud de la línea diagonal (de esquina superior izquierda a inferior derecha)
				const diagonalLength = Math.sqrt(Math.pow(ghostDiv.clientWidth, 2) + Math.pow(ghostDiv.clientHeight, 2));

				linea1.style.width = `${diagonalLength}px`;
				linea1.style.transform = `rotate(${Math.atan2(ghostDiv.clientHeight, ghostDiv.clientWidth)}rad)`;

				linea2.style.width = `${diagonalLength}px`;
				linea2.style.transform = `rotate(${-Math.atan2(ghostDiv.clientHeight, ghostDiv.clientWidth)}rad)`;

				// Colocar la línea en la esquina superior derecha
				linea2.style.right = '0px';
				linea2.style.top = '0px';
			};

			switch (tiradorId) {
				case 'tirador-tl':
					// Mueve la esquina superior izquierda
					ghostDiv.style.left = `${ghostDiv.offsetLeft + difX}px`;
					ghostDiv.style.top = `${ghostDiv.offsetTop + difY}px`;

					// Mueve la esquina inferior derecha
					ghostDiv.style.width = `${ghostDiv.offsetWidth - difX}px`;
					ghostDiv.style.height = `${ghostDiv.offsetHeight - difY}px`;
					recalculaDiagonales();
					break;

				case 'tirador-tr':
					// Mueve la esquina superior derecha
					ghostDiv.style.top = `${ghostDiv.offsetTop + difY}px`;

					// Mueve la esquina inferior izquierda
					ghostDiv.style.width = `${ghostDiv.offsetWidth + difX}px`;
					ghostDiv.style.height = `${ghostDiv.offsetHeight - difY}px`;
					recalculaDiagonales();
					break;

				case 'tirador-bl':
					// Mueve la esquina inferior izquierda
					ghostDiv.style.left = `${ghostDiv.offsetLeft + difX}px`;
					ghostDiv.style.height = `${ghostDiv.offsetHeight + difY}px`;

					// Mueve la esquina superior derecha
					ghostDiv.style.width = `${ghostDiv.offsetWidth - difX}px`;
					recalculaDiagonales();
					break;

				case 'tirador-br':
					// Mueve la esquina inferior derecha
					ghostDiv.style.width = `${ghostDiv.offsetWidth + difX}px`;
					ghostDiv.style.height = `${ghostDiv.offsetHeight + difY}px`;
					recalculaDiagonales();
					break;

				case 'tirador-center':
					ghostDiv.style.left = `${ghostDiv.offsetLeft + difX}px`;
					ghostDiv.style.top = `${ghostDiv.offsetTop + difY}px`;
					break;
				default:
					console.error('Tirador desconocido');
					break;
			}
			// Actualiza las posiciones del mouse para el próximo movimiento
			posicionInicial.x = $event2.clientX;
			posicionInicial.y = $event2.clientY;

			// Actualiza las posiciones de la cruz de posicionamiento
			// Calcular el centro del ghost para posicionar las lineas
			intersecciones = this.colisiones(ghostDiv);
			const rect = this.canvas.getBoundingClientRect();
			const centroX = ghostDiv.offsetLeft + ghostDiv.offsetWidth / 2 + rect.x;
			const centroY = ghostDiv.offsetTop + ghostDiv.offsetHeight / 2 + rect.y;
			this.moverCruzPosicionamiento(centroX, centroY, intersecciones);

			intersecciones?.forEach((elemento) => {
				if (elemento.id === 'canvas-container') {
					if (this.canvas) {
						this.canvas.style.border = '2px solid #b91c1c';
					}
				} else {
					elemento.style.border = '2px solid #b91c1c';
					elemento.style.visibility = 'visible';
				}
			});
		};
		canvasContainer.addEventListener('mousemove', mouseMove);

		// Evento mouseup
		const mouseup = () => {
			if (!this.canvas) return;
			const elemento: VideoElement | undefined = this.videosElements.find((el) => el.id === ghostId.substring(6));
			if (!elemento) return;
			const ghostRect = ghostDiv.getBoundingClientRect();
			const result: VideoElement | undefined = this.paintInCanvas(elemento.element, ghostRect.width, ghostRect.height, ghostRect.left + ghostRect.width / 2, ghostRect.top + ghostRect.height / 2);
			// Guardar datos en el objeto VideoElement
			if (!result) return;
			elemento.position = result.position;
			elemento.scale = result.scale; // Escala que garantiza el tamaño correcto en el canvas
			elemento.painted = true; // Marcamos el video como "pintado"

			// Restaurar estado
			canvasContainer.removeEventListener('mousemove', mouseMove);
			canvasContainer.removeEventListener('mouseup', mouseup);
			cross.style.display = 'none';
			const elementos = canvasContainer.querySelectorAll('[id^="marco"]') as NodeListOf<HTMLDivElement>;
			elementos.forEach((elemento) => {
				if (elemento.id !== ghostDiv.id) {
					elemento.style.border = '1px solid black';
				}
			});
			this.canvas.style.border = '1px solid black';
			ghostDiv.style.visibility = 'hidden';
			this.editandoDimensiones = false;
		};
		canvasContainer.addEventListener('mouseup', mouseup);
	}

	paintInCanvas(element: HTMLElement, widthElement: number, heightElement: number, positionX: number, positionY: number) {
		if (!this.canvas) return;
		const rect = this.canvas.getBoundingClientRect();

		// Relación de escala entre el tamaño visual y el interno del canvas
		const scaleX = this.canvas.width / rect.width;
		const scaleY = this.canvas.height / rect.height;

		// Dimensiones del ghost en el documento
		const ghostWidthInCanvas = widthElement * scaleX; // Ajustado al canvas
		const ghostHeightInCanvas = heightElement * scaleY;

		// Dimensiones originales del video
		let originalWidth: number = 0;
		let originalHeight: number = 0;
		if (element instanceof HTMLVideoElement) {
			originalWidth = element.videoWidth;
			originalHeight = element.videoHeight;
		} else if (element instanceof HTMLImageElement) {
			originalWidth = element.naturalWidth;
			originalHeight = element.naturalHeight;
		} else {
			console.error('Tipo de elemento no reconocido');
			return;
		}

		// Calculamos la escala requerida
		const requiredScaleX = ghostWidthInCanvas / originalWidth;
		const requiredScaleY = ghostHeightInCanvas / originalHeight;
		const requiredScale = Math.min(requiredScaleX, requiredScaleY);

		// Dimensiones escaladas
		const scaledWidth = originalWidth * requiredScale;
		const scaledHeight = originalHeight * requiredScale;

		// Ajustamos la posición para centrar el ratón en el video escalado
		const canvasX = (positionX - rect.left) * scaleX - scaledWidth / 2;
		const canvasY = (positionY - rect.top) * scaleY - scaledHeight / 2;

		// Devuelve un VideoElement con la información de la imagen da pintar
		const videoElement: VideoElement = {
			id: element.id,
			element: element,
			painted: true,
			scale: requiredScale,
			position: { x: canvasX, y: canvasY },
		};
		return videoElement;
	}

	colisiones(principal: HTMLElement): HTMLElement[] {
		const rect = principal.getBoundingClientRect();
		const elementosIntersecados: HTMLElement[] = [];
		const canvasContainer = document.getElementById('canvas-container') as HTMLElement;
		if (!canvasContainer || !this.canvas) return elementosIntersecados;
		const elementos = canvasContainer.querySelectorAll('[id^="marco"]') as NodeListOf<HTMLElement>;
		elementos.forEach((elemento) => {
			if (elemento.id != principal.id) {
				const rect2 = elemento.getBoundingClientRect();
				// Comprobamos si rect se intersecta con rect2
				const intersecta = rect.left < rect2.right && rect.right > rect2.left && rect.top < rect2.bottom && rect.bottom > rect2.top;
				if (intersecta) {
					elementosIntersecados.push(elemento);
				}
			}
		});
		const canvasRect = this.canvas.getBoundingClientRect();
		const tocaBorde = rect.left <= canvasRect.left || rect.right >= canvasRect.right || rect.top <= canvasRect.top || rect.bottom >= canvasRect.bottom;
		if (tocaBorde) {
			elementosIntersecados.push(canvasContainer);
		}
		return elementosIntersecados;
	}

	private formatTime(seconds: number): string {
		const mins = Math.floor(seconds / 60);
		const secs = Math.floor(seconds % 60);
		return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
	}

	guardaPreset() {
		const name = prompt('Introduce el nombre del preset', 'Nuevo preset');
		if (name) {
			console.log(name);
			const videoElements: VideoElement[] = [];
			this.videosElements.forEach((elemento) => {
				if (elemento.painted) {
					videoElements.push(elemento);
				}
			});
			this.presets.set(name, { elements: videoElements, shortcut: 'ctrl+' + this.presets.size + 1 });
			setTimeout(() => this.calculatePreset(), 100);
		}
	}

	stopElemento(ele: MediaDeviceInfo | MediaStream | File) {
		if (ele instanceof MediaDeviceInfo) {
			const div = document.getElementById('div-' + ele.deviceId);
			if (div) {
				const videoElement = div.querySelector('video') as HTMLVideoElement;
				if (videoElement) {
					const stream = videoElement.srcObject as MediaStream;
					stream.getTracks().forEach((track) => track.stop());
				}
			}
			this.videoDevices = this.videoDevices.filter((device) => device.deviceId !== ele.deviceId);
		} else if (ele instanceof MediaStream) {
			const div = document.getElementById('div-' + ele.id);
			if (div) {
				const videoElement = div.querySelector('video') as HTMLVideoElement;
				if (videoElement) {
					const stream = videoElement.srcObject as MediaStream;
					stream.getTracks().forEach((track) => track.stop());
				}
			}
			this.capturas = this.capturas.filter((stream) => stream !== ele);
		} else if (ele instanceof File) {
			this.staticContent = this.staticContent.filter((file) => file !== ele);
		}
	}

	fullscreen(ele: MediaDeviceInfo | MediaStream | File) {
		let elemento: VideoElement | undefined;
		if (ele instanceof MediaDeviceInfo) {
			elemento = this.videosElements.find((el) => el.id === ele.deviceId);
		} else if (ele instanceof MediaStream) {
			elemento = this.videosElements.find((el) => el.id === ele.id);
		} else if (ele instanceof File) {
			elemento = this.videosElements.find((el) => el.id === ele.name);
		} else return;
		if (!elemento || !elemento.element || !this.canvas) return;
		const rect = this.canvas.getBoundingClientRect();
		const x = rect.x + rect.width / 2;
		const y = rect.y + rect.height / 2;
		const result = this.paintInCanvas(elemento.element, rect.width, rect.height, x, y);
		if (result && elemento.element) {
			elemento.position = result.position;
			elemento.scale = result.scale;
			elemento.painted = true;
			this.addCapa(elemento);
		}
	}

	addCapa(elemento: VideoElement) {
		const div = document.getElementById('div-' + elemento.id);
		if (div) {
			const capa: HTMLDivElement = document.getElementById('capa')?.cloneNode(true) as HTMLDivElement;
			capa.id = 'capa-' + elemento.id;
			capa.classList.remove('hidden');

			// Botón para detener la emisión
			const X: HTMLButtonElement = capa.querySelector('#buttonxcapa') as HTMLButtonElement;
			X.onclick = () => {
				if (!elemento) return;
				elemento.painted = false;
				elemento.position = null;
				elemento.scale = 1;
				div.removeChild(capa);
				const marco = document.getElementById('marco-' + elemento.id);
				if (marco) {
					marco.remove();
				}
			};

			if (elemento.element instanceof HTMLVideoElement && elemento.element.src && elemento.element.src.length > 0) {
				const control = document.getElementById('control')?.cloneNode(true) as HTMLDivElement;
				if (!control) return;
				const controllers = capa.querySelector('#controllers') as HTMLDivElement;
				if (!controllers) return;
				control.id = 'control-' + elemento.id;
				control.style.display = 'block';
				const playPause = control.querySelector('#play-pause') as HTMLButtonElement;
				const restart = control.querySelector('#restart') as HTMLButtonElement;
				const loop = control.querySelector('#loop') as HTMLButtonElement;
				const play = control.querySelector('#play') as SVGElement;
				const pause = control.querySelector('#pause') as SVGElement;
				const loopOff = control.querySelector('#loop-off') as SVGElement;
				const loopOn = control.querySelector('#loop-on') as SVGElement;
				const progress = control.querySelector('#progress') as HTMLInputElement;
				const time = control.querySelector('#time') as HTMLSpanElement;

				playPause.onclick = () => {
					if (!elemento) return;
					if ((elemento.element as HTMLVideoElement).paused) {
						(elemento.element as HTMLVideoElement).play();
						play.style.display = 'none';
						pause.style.display = 'block';
					} else {
						(elemento.element as HTMLVideoElement).pause();
						play.style.display = 'block';
						pause.style.display = 'none';
					}
				};

				restart.onclick = () => {
					if (!elemento) return;
					(elemento.element as HTMLVideoElement).currentTime = 0;
				};

				loop.onclick = () => {
					if (!elemento) return;
					if ((elemento.element as HTMLVideoElement).loop) {
						(elemento.element as HTMLVideoElement).loop = false;
						loopOff.style.display = 'block';
						loopOn.style.display = 'none';
					} else {
						(elemento.element as HTMLVideoElement).loop = true;
						loopOff.style.display = 'none';
						loopOn.style.display = 'block';
					}
				};

				/* Barra de progreso */
				elemento.element.ontimeupdate = () => {
					if (!elemento) return;
					const percentage = ((elemento.element as HTMLVideoElement).currentTime / (elemento.element as HTMLVideoElement).duration) * 100;
					progress.value = percentage.toString();
					// Mostrar el tiempo actual y la duración
					const currentTime = this.formatTime((elemento.element as HTMLVideoElement).currentTime);
					const duration = this.formatTime((elemento.element as HTMLVideoElement).duration);
					time.innerText = `${currentTime} / ${duration}`;

					progress.oninput = () => {
						if (!elemento) return;
						const newTime = (parseInt(progress.value) / 100) * (elemento.element as HTMLVideoElement).duration;
						(elemento.element as HTMLVideoElement).currentTime = newTime;

						// Actualizar el tiempo en el texto inmediatamente
						const currentTime = this.formatTime((elemento.element as HTMLVideoElement).currentTime);
						const duration = this.formatTime((elemento.element as HTMLVideoElement).duration);
						time.innerText = `${currentTime} / ${duration}`;
					};
				};

				/* Tiempo de reproducción */
				elemento.element.addEventListener('timeupdate', () => {
					if (!elemento) return;
					const currentTime = this.formatTime((elemento.element as HTMLVideoElement).currentTime);
					const duration = this.formatTime((elemento.element as HTMLVideoElement).duration);
					time.innerText = `${currentTime} / ${duration}`;
				});
				const currentTime = this.formatTime((elemento.element as HTMLVideoElement).currentTime);
				const duration = this.formatTime((elemento.element as HTMLVideoElement).duration);
				time.innerText = `${currentTime} / ${duration}`;
				controllers.appendChild(control);
			}

			div.appendChild(capa);
		}
	}

	moverCruzPosicionamiento(eventX: number, eventY: number, intersecciones: HTMLElement[]) {
		const cross = document.getElementById('cross') as HTMLDivElement;
		if (!cross || !this.canvas) return;
		const rect = this.canvas.getBoundingClientRect();
		const orizontal = cross.querySelector('#orizontal') as HTMLDivElement;
		orizontal.style.display = 'none';
		orizontal.style.backgroundColor = '#1d4ed8';
		orizontal.style.width = rect.width + 'px';
		const vertical = cross.querySelector('#vertical') as HTMLDivElement;
		vertical.style.display = 'none';
		vertical.style.backgroundColor = '#1d4ed8';
		vertical.style.height = rect.height + 'px';

		const isMouseOverCanvas: boolean = eventX >= rect.left && eventX <= rect.right && eventY >= rect.top && eventY <= rect.bottom;

		const cursorPosition = {
			isAbove: eventY < rect.top,
			isBelow: eventY > rect.bottom,
			isLeft: eventX < rect.left,
			isRight: eventX > rect.right,
		};
		if ((cursorPosition.isLeft || cursorPosition.isRight) && !cursorPosition.isAbove && !cursorPosition.isBelow) {
			orizontal.style.display = 'block';
		}
		if ((cursorPosition.isAbove || cursorPosition.isBelow) && !cursorPosition.isLeft && !cursorPosition.isRight) {
			vertical.style.display = 'block';
		}

		if (isMouseOverCanvas) {
			orizontal.style.display = 'block';
			vertical.style.display = 'block';
		}

		if (intersecciones && intersecciones.length > 0) {
			orizontal.style.backgroundColor = '#b91c1c';
			vertical.style.backgroundColor = '#b91c1c';
		}
		vertical.style.left = `${eventX - rect.left}px`;
		orizontal.style.top = `${eventY - rect.top}px`;
	}

	calculatePreset() {
		const keysArray = Array.from(this.presets.keys());
		keysArray.forEach((key) => {
			const presetDiv = document.getElementById('preset-' + key);
			if (!presetDiv) {
				console.error('Preset ' + key + ' no encontrado');
				return;
			}
			presetDiv.innerHTML = '';
			this.presets.get(key)?.elements.forEach((element) => {
				let ele;
				if (element.element instanceof HTMLVideoElement) {
					ele = document.createElement('video');
					const originalStream = element.element.srcObject as MediaStream;

					if (originalStream) {
						// Crear un nuevo flujo vacío
						const newStream = new MediaStream();

						// Copiar todas las pistas (video, audio) al nuevo flujo
						originalStream.getTracks().forEach((track) => {
							newStream.addTrack(track);
						});

						// Asignar el nuevo flujo al video
						ele.srcObject = newStream;
						ele.autoplay = true;
						ele.muted = true;
					}
				} else if (element.element instanceof HTMLImageElement) {
					ele = document.createElement('img');
					ele.src = element.element.src;
					ele.alt = element.element.id;
				} else return;

				// Calculamos la escala y posición en el div respecto al canvas
				const divRect = presetDiv.getBoundingClientRect();
				if (!this.canvas || !element.position) return;
				// Relación de escala entre el tamaño interno del canvas y el tamaño del div
				const scaleX = this.canvas.width / divRect.width;
				const scaleY = this.canvas.height / divRect.height;
				// Calculamos la posición en el div
				ele.classList.add('absolute');
				ele.style.left = `${element.position.x / scaleX}px`;
				ele.style.top = `${element.position.y / scaleY}px`;
				ele.style.width = `${element.scale * divRect.width}px`;
				ele.style.height = `${element.scale * divRect.height}px`;
				presetDiv.appendChild(ele);
			});
		});
	}
}
export interface VideoElement {
	id: string;
	element: HTMLElement;
	painted: boolean;
	scale: number;
	position: { x: number; y: number } | null;
}
