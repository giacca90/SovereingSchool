/* import { Component } from '@angular/core';

@Component({
  selector: 'app-reproduction',
  standalone: true,
  imports: [],
  templateUrl: './reproduction.component.html',
  styleUrl: './reproduction.component.css'
})
export class ReproductionComponent {

} */

import { Component } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { StreamingService } from '../../services/streaming.service';

@Component({
	selector: 'app-reproduction',
	standalone: true,
	imports: [],
	templateUrl: './reproduction.component.html',
	styleUrl: './reproduction.component.css',
})
export class ReproductionComponent {
	videoUrl: SafeUrl | undefined;

	constructor(
		private streamingService: StreamingService,
		private sanitizer: DomSanitizer,
	) {}

	ngOnInit(): void {
		this.streamingService.getVideo('your-video-file.mp4').subscribe((blob: Blob) => {
			this.videoUrl = this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(blob));
		});
	}
}

/* Explicación del componente:
videoUrl: SafeUrl | undefined: Almacena la URL segura para el video.
DomSanitizer: Servicio de Angular que ayuda a sanitizar URLs para evitar problemas de seguridad.
ngOnInit(): Método del ciclo de vida del componente que se ejecuta después de la inicialización del componente.
subscribe(blob => { ... }): Maneja la respuesta de la solicitud de video y convierte el blob en una URL segura para el video.
 */
