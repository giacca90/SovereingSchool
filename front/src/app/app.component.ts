import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { LogModalComponent } from './components/log-modal/log-modal.component';
import { SearchComponent } from './components/search/search.component';
import { InitService } from './services/init.service';
import { LoginModalService } from './services/login-modal.service';
import { LoginService } from './services/login.service';

@Component({
	selector: 'app-root',
	standalone: true,
	templateUrl: './app.component.html',
	styleUrl: './app.component.css',
	imports: [RouterOutlet, SearchComponent, LogModalComponent, CommonModule],
})
export class AppComponent implements OnInit, OnDestroy {
	title = 'Sovereign School';
	isModalVisible: boolean = false;
	vistaMenu: boolean = false;
	private subscription: Subscription = new Subscription();

	constructor(
		private modalService: LoginModalService,
		private initService: InitService,
		public loginService: LoginService,
		public router: Router,
	) {}

	ngOnInit() {
		// Suscribirse a los cambios del tema del sistema
		window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
			if (!('theme' in localStorage)) {
				document.documentElement.classList.toggle('dark', e.matches);
			}
		});

		this.subscription.add(
			this.modalService.isVisible$.subscribe((isVisible) => {
				this.isModalVisible = isVisible;
			}),
		);
	}

	openModal() {
		this.modalService.show();
	}

	salir() {
		this.vistaMenu = false;
		this.loginService.usuario = null;
		localStorage.clear();
		this.router.navigate(['']);
	}
	ngOnDestroy(): void {
		this.subscription.unsubscribe();
	}

	changeTheme() {
		const isDark = localStorage.getItem('theme') === 'dark';
		const newTheme = isDark ? 'light' : 'dark';
		localStorage.setItem('theme', newTheme);
		document.documentElement.classList.toggle('dark', newTheme === 'dark');
	}
}
