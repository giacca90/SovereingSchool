import { isPlatformBrowser } from '@angular/common';
import { AfterViewInit, Component, Inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import videojs from 'video.js';
import { Clase } from '../../models/Clase';
import { Curso } from '../../models/Curso';
import { CursosService } from '../../services/cursos.service';

@Component({
	selector: 'app-reproduction',
	standalone: true,
	imports: [],
	templateUrl: './reproduction.component.html',
	styleUrl: './reproduction.component.css',
})
export class ReproductionComponent implements OnInit, AfterViewInit, OnDestroy {
	public id_usuario: number = 0;
	public id_curso: number = 0;
	public id_clase: number = 0;
	private isBrowser: boolean;
	private subscription: Subscription = new Subscription();
	public loading: boolean = true;
	public curso: Curso | null = null;
	public clase: Clase | null = null;
	constructor(
		private route: ActivatedRoute,
		@Inject(PLATFORM_ID) private platformId: object,
		public cursoService: CursosService,
		public router: Router,
	) {
		this.isBrowser = isPlatformBrowser(platformId);
	}

	ngOnInit(): void {
		this.subscription.add(
			this.route.params.subscribe((params) => {
				this.id_usuario = params['id_usuario'];
				this.id_curso = params['id_curso'];
				this.id_clase = params['id_clase'];
				if (this.id_clase == 0) {
					this.cursoService.getStatusCurso(this.id_usuario, this.id_curso).subscribe({
						next: (resp) => {
							if (resp === 0) {
								this.router.navigate(['/']);
							} else {
								this.router.navigate(['repro/' + this.id_usuario + '/' + this.id_curso + '/' + resp]);
							}
						},
					});
				} else {
					this.loadData();
				}
			}),
		);
	}

	loadData() {
		this.cursoService.getCurso(this.id_curso).then((result) => {
			this.curso = result;
			if (this.curso && this.curso.clases_curso) {
				const result = this.curso.clases_curso.find((clase) => clase.id_clase == this.id_clase);
				if (result) {
					this.clase = result;
				}
			}
		});
	}

	ngAfterViewInit(): void {
		if (this.isBrowser) {
			this.getVideo();
		}
	}

	private async getVideo() {
		try {
			const video: HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;

			if (video) {
				const player = videojs(video, {
					controls: true,
					autoplay: true,
					preload: 'auto',
				});
				player.src({
					src: `http://localhost:8090/${this.id_usuario}/${this.id_curso}/${this.id_clase}/master.m3u8`,
					type: 'application/x-mpegURL',
				});

				this.loading = false;
			}
		} catch (error) {
			console.error('Error loading video:', error);
		}
	}
	ngOnDestroy(): void {
		this.subscription.unsubscribe();
	}

	navega(clase: Clase) {
		this.router.navigate(['/']).then(() => {
			this.router.navigate(['repro/' + this.id_usuario + '/' + this.id_curso + '/' + clase.id_clase]);
		});
	}
}
