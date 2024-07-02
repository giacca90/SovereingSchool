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
