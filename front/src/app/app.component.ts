import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { HomeComponent } from './components/home/home.component';
import { LogModalComponent } from './components/log-modal/log-modal.component';
import { SearchComponent } from './components/search/search.component';
import { Usuario } from './models/Usuario';
import { InitService } from './services/init.service';
import { LoginModalService } from './services/login-modal.service';
import { LoginService } from './services/login.service';

@Component({
	selector: 'app-root',
	standalone: true,
	templateUrl: './app.component.html',
	styleUrl: './app.component.css',
	imports: [RouterOutlet, HomeComponent, SearchComponent, LogModalComponent, CommonModule],
})
export class AppComponent implements OnInit, OnDestroy {
	title = 'Sovereign School';
	isModalVisible: boolean = false;
	vistaMenu: boolean = false;
	private subscription: Subscription = new Subscription();
	usuario: Usuario | null = null;

	constructor(
		private modalService: LoginModalService,
		private initService: InitService,
		private cdr: ChangeDetectorRef,
		public loginService: LoginService,
		public router: Router,
	) {}

	ngOnInit() {
		this.subscription.add(
			this.modalService.isVisible$.subscribe((isVisible) => {
				this.isModalVisible = isVisible;
			}),
		);

		this.subscription.add(
			this.loginService.usuario$.subscribe((usuario) => {
				this.usuario = usuario;
				this.cdr.detectChanges();
			}),
		);
	}

	openModal() {
		this.modalService.show();
	}

	salir() {
		this.vistaMenu = false;
		this.loginService.salir();
		this.router.navigate(['']);
	}
	ngOnDestroy(): void {
		this.subscription.unsubscribe();
	}
}
