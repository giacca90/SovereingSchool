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
	editandoDimensiones = false; // Indica si se está editando las dimensiones de un video
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
				// Eliminar el objeto ele del array videosElements
				this.videosElements = this.videosElements.filter((v) => v.id !== stream.id);
			};
		} catch (error) {
			console.error('Error al capturar ventana o pantalla:', error);
		}
	}

	mousedown(event: MouseEvent, deviceId: string): void {
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
		const canvasContainer = document.getElementById('canvas-container') as HTMLDivElement;
		const crossCopy = document.getElementById('cross')?.cloneNode(true) as HTMLDivElement;
		if (!canvasContainer || !crossCopy) return;
		canvasContainer.appendChild(crossCopy);

		const mousemove = (moveEvent: MouseEvent) => {
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
						ghost.style.border = '2px solid #1d4ed8';
						this.canvas.style.border = '2px solid #1d4ed8';
					} else {
						ghost.style.border = '2px solid #b91c1c';
						this.canvas.style.border = '2px solid #b91c1c';
					}
				} else {
					ghost.style.clipPath = 'none'; // Restaurar si no hay intersección
					ghost.classList.add('ghost-video');
					ghost.style.border = '1px solid black';
					this.canvas.style.border = '1px solid black';
				}

				const isMouseOverCanvas: boolean = moveEvent.clientX >= rect.left && moveEvent.clientX <= rect.right && moveEvent.clientY >= rect.top && moveEvent.clientY <= rect.bottom;
				const orizontal = crossCopy.querySelector('#orizontal') as HTMLDivElement;
				orizontal.style.display = 'none';

				const vertical = crossCopy.querySelector('#vertical') as HTMLDivElement;
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
						orizontal.style.display = 'block';
					}
					if ((cursorPosition.isAbove || cursorPosition.isBelow) && !cursorPosition.isLeft && !cursorPosition.isRight) {
						vertical.style.display = 'block';
					}

					if (isMouseOverCanvas) {
						vertical.style.display = 'block';
						orizontal.style.display = 'block';
					}

					if (isFullyContained) {
						vertical.style.backgroundColor = '#1d4ed8';
						orizontal.style.backgroundColor = '#1d4ed8';
					} else {
						vertical.style.backgroundColor = '#b91c1c';
						orizontal.style.backgroundColor = '#b91c1c';
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

				// Quita la cruz de posicionamiento
				const cross = document.getElementById('cross') as HTMLDivElement;
				if (cross) {
					cross.style.display = 'none';
				}

				// Añade una capa encima al elemento transmitido
				const div = document.getElementById('div-' + ele.id);
				if (div) {
					const capa: HTMLDivElement = document.getElementById('capa')?.cloneNode(true) as HTMLDivElement;
					capa.id = 'capa-' + ele.id;
					capa.classList.remove('hidden');

					// Botón para detener la emisión
					const X: HTMLButtonElement = capa.querySelector('#buttonxcapa') as HTMLButtonElement;
					X.onclick = () => {
						ele.painted = false;
						ele.position = null;
						ele.scale = 1;
						div.removeChild(capa);
					};
					div.appendChild(capa);
				}
			}

			// Restaurar estado
			this.canvas.style.border = '1px solid black';
			this.dragVideo = null;
			ghost.style.visibility = 'hidden';
			crossCopy.remove();
			document.removeEventListener('mousemove', mousemove);
			document.removeEventListener('wheel', wheel);
			document.removeEventListener('mouseup', mouseup);
		};
		document.addEventListener('mouseup', mouseup);
	}

	canvasMouseMove(event: MouseEvent) {
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
			const videoWidth = video.element.videoWidth * video.scale;
			const videoHeight = video.element.videoHeight * video.scale;

			// Coordenadas del video en el canvas
			const videoLeft = video.position ? video.position.x : 0;
			const videoTop = video.position ? video.position.y : 0;

			// Comprobar si el ratón está dentro del área del video
			const isMouseOverVideo = internalMouseX >= videoLeft && internalMouseX <= videoLeft + videoWidth && internalMouseY >= videoTop && internalMouseY <= videoTop + videoHeight;

			// Buscar el elemento "ghost"
			let ghostDiv = canvasContainer.querySelector('#marco-' + video.id) as HTMLDivElement;
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
				canvasContainer.appendChild;

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
		const cross = document.getElementById('cross')?.cloneNode(true) as HTMLDivElement;
		if (!cross) return;
		const intersecciones = this.colisiones(ghostDiv as HTMLDivElement);
		console.log('intersecciones ', intersecciones);
		cross.style.left = this.canvas.offsetLeft + 'px';
		cross.style.top = this.canvas.offsetTop + 'px';
		cross.style.width = this.canvas.offsetWidth + 'px';
		cross.style.height = this.canvas.offsetHeight + 'px';
		if (intersecciones && intersecciones.length > 0) {
			cross.style.border = '2px solid #b91c1c';
		} else {
			cross.style.border = '2px solid #1d4ed8';
		}
		cross.style.display = 'block';
		canvasContainer.appendChild(cross);
		const orizontal = cross.querySelector('#orizontal') as HTMLDivElement;
		const vertical = cross.querySelector('#vertical') as HTMLDivElement;
		if (!orizontal || !vertical) return;
		orizontal.style.width = this.canvas.offsetWidth - 2 + 'px';
		orizontal.style.left = this.canvas.offsetLeft + 'px';
		if (intersecciones && intersecciones.length > 0) {
			orizontal.style.backgroundColor = '#b91c1c';
		} else {
			orizontal.style.backgroundColor = '#1d4ed8';
		}
		orizontal.style.display = 'block';
		vertical.style.height = this.canvas.offsetHeight - 2 + 'px';
		vertical.style.top = this.canvas.offsetTop + 'px';
		if (intersecciones && intersecciones.length > 0) {
			vertical.style.backgroundColor = '#b91c1c';
		} else {
			vertical.style.backgroundColor = '#1d4ed8';
		}
		vertical.style.display = 'block';
		// Calcular el centro del ghost para posicionar las lineas
		const centroX = ghostDiv.offsetLeft + ghostDiv.offsetWidth / 2;
		const centroY = ghostDiv.offsetTop + ghostDiv.offsetHeight / 2;
		orizontal.style.top = centroY + 'px';
		vertical.style.left = centroX + 'px';

		// En evento mousemove, se calcula la diferencia de posición entre el momento del click y el movimiento
		const mouseMove = ($event2: MouseEvent) => {
			const difX = $event2.clientX - posicionInicial.x;
			const difY = $event2.clientY - posicionInicial.y;
			if (!this.canvas) return;
			this.canvas.style.border = '1px solid black';
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
			const centroX = ghostDiv.offsetLeft + ghostDiv.offsetWidth / 2;
			const centroY = ghostDiv.offsetTop + ghostDiv.offsetHeight / 2;
			orizontal.style.top = centroY + 'px';
			vertical.style.left = centroX + 'px';
			const intersecciones = this.colisiones(ghostDiv as HTMLDivElement);
			console.log('intersecciones ', intersecciones);

			// Cambiar el color de la cruz de posicionamiento si hay alguna colisión
			if (intersecciones && intersecciones.length > 0) {
				// Pone el marco rojo a los elementos colisionados
				intersecciones.forEach((elemento) => {
					if (elemento.id === 'canvas-container') {
						if (this.canvas) {
							this.canvas.style.border = '2px solid #b91c1c';
						}
					} else {
						elemento.style.border = '2px solid #b91c1c';
						elemento.style.visibility = 'visible';
					}
				});
				orizontal.style.backgroundColor = '#b91c1c';
				vertical.style.backgroundColor = '#b91c1c';
				ghostDiv.style.border = '2px solid #b91c1c';
			} else {
				orizontal.style.backgroundColor = '#1d4ed8';
				vertical.style.backgroundColor = '#1d4ed8';
				ghostDiv.style.border = '2px solid #1d4ed8';
			}
		};
		canvasContainer.addEventListener('mousemove', mouseMove);

		// Evento mouseup
		const mouseup = () => {
			if (!this.canvas) return;
			const rect = this.canvas.getBoundingClientRect();
			// Relación de escala entre el tamaño visual y el interno del canvas
			const scaleX = this.canvas.width / rect.width;
			const scaleY = this.canvas.height / rect.height;

			// Dimensiones del ghost en el documento
			const ghostRect = ghostDiv.getBoundingClientRect();
			const ghostWidthInCanvas = ghostRect.width * scaleX; // Ajustado al canvas
			const ghostHeightInCanvas = ghostRect.height * scaleY;

			// Dimensiones originales del video
			const elemento: VideoElement | undefined = this.videosElements.find((el) => el.id === ghostId.substring(6));
			if (!elemento) return;
			const originalWidth = elemento.element.videoWidth;
			const originalHeight = elemento.element.videoHeight;

			// Calculamos la escala requerida
			const requiredScaleX = ghostWidthInCanvas / originalWidth;
			const requiredScaleY = ghostHeightInCanvas / originalHeight;
			const requiredScale = Math.min(requiredScaleX, requiredScaleY);

			// Dimensiones escaladas
			const scaledWidth = originalWidth * requiredScale;
			const scaledHeight = originalHeight * requiredScale;

			// Ajustamos la posición para centrar el ratón en el video escalado
			const center = { x: ghostRect.left + ghostRect.width / 2, y: ghostRect.top + ghostRect.height / 2 };
			const canvasX = (center.x - rect.left) * scaleX - scaledWidth / 2;
			const canvasY = (center.y - rect.top) * scaleY - scaledHeight / 2;

			// Guardar datos en el objeto VideoElement
			elemento.position = { x: canvasX, y: canvasY };
			elemento.scale = requiredScale; // Escala que garantiza el tamaño correcto en el canvas
			elemento.painted = true; // Marcamos el video como "pintado"

			// Restaurar estado
			canvasContainer.removeEventListener('mousemove', mouseMove);
			canvasContainer.removeEventListener('mouseup', mouseup);
			cross.remove();
			ghostDiv.style.visibility = 'hidden';
			this.editandoDimensiones = false;
		};
		canvasContainer.addEventListener('mouseup', mouseup);
	}

	colisiones(principal: HTMLDivElement) {
		const rect = principal.getBoundingClientRect();
		const elementosIntersecados: HTMLDivElement[] = [];
		const canvasContainer = document.getElementById('canvas-container') as HTMLDivElement;
		if (!canvasContainer || !this.canvas) return;
		const elementos = canvasContainer.querySelectorAll('[id^="marco"]') as NodeListOf<HTMLDivElement>;
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
			console.log('toca borde: ' + canvasContainer.id);
			elementosIntersecados.push(canvasContainer);
		}
		return elementosIntersecados;
	}
}
export interface VideoElement {
	id: string;
	element: HTMLVideoElement;
	painted: boolean;
	scale: number;
	position: { x: number; y: number } | null;
}
