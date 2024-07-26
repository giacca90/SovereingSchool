import { isPlatformBrowser } from '@angular/common';
import { AfterViewInit, Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { map } from 'rxjs';

@Component({
	selector: 'app-reproduction',
	standalone: true,
	imports: [],
	templateUrl: './reproduction.component.html',
	styleUrl: './reproduction.component.css',
})
export class ReproductionComponent implements OnInit, AfterViewInit {
	private id_usuario: number = 0;
	private id_curso: number = 0;
	private id_clase: number = 0;
	private isBrowser: boolean;
	public loading: boolean = true;
	constructor(
		private route: ActivatedRoute,
		@Inject(PLATFORM_ID) private platformId: object,
	) {
		this.isBrowser = isPlatformBrowser(platformId);
	}

	ngOnInit(): void {
		this.route.params.pipe(
			map((params) => {
				this.id_usuario = params['id_usuario'];
				this.id_curso = params['id_curso'];
				this.id_clase = params['id_clase'];
			}),
		);
	}

	ngAfterViewInit(): void {
		if (this.isBrowser) {
			this.getVideo();
		}
	}

	private async getVideo() {
		try {
			const video: HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;
			const xhr = new XMLHttpRequest();
			xhr.open('GET', `http://localhost:8090/${this.id_usuario}/${this.id_curso}/${this.id_clase}`);
			xhr.responseType = 'blob';
			xhr.onload = () => {
				if (xhr.status === 200) {
					const videoBlob = xhr.response;
					const videoUrl = URL.createObjectURL(videoBlob);
					video.src = videoUrl;
					this.loading = false;
					video.play();
				} else {
					console.error('Failed to load video:', xhr.status, xhr.statusText);
				}
			};
			xhr.onerror = () => {
				console.error('Network error while loading video.');
			};
			xhr.send();
		} catch (error) {
			console.error('Error loading video:', error);
		}
	}
}
