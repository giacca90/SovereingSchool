import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class StreamingService {
	private URL: string = 'http://localhost:8090';

	constructor(private http: HttpClient) {}

	getVideo(id_usuario: number, id_curso: number, id_clase: number): Observable<Blob> {
		return this.http.get(`${this.URL}/${id_usuario}/${id_curso}/${id_clase}`, { responseType: 'blob' });
	}
}
