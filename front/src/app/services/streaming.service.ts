import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
	providedIn: 'root',
})
export class StreamingService {
	private URL: String = 'http://localhost:8090';

	constructor(private http: HttpClient) {}

	getVideo(filename: string) {
		const headers = new HttpHeaders({
			'Content-Type': 'application/octet-stream',
		});
		return this.http.get(`${this.URL}/videos/${filename}`, { headers, responseType: 'blob' });
	}
}

/* Explicación del servicio:
HttpClient: Servicio de Angular para hacer solicitudes HTTP.
HttpHeaders: Se usa para especificar los encabezados de la solicitud HTTP.
getVideo(filename: string): Método que hace una solicitud GET para obtener el archivo de video especificado. */
