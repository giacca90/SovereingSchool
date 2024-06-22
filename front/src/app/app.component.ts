import { CommonModule } from '@angular/common'; // Importa CommonModule
import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LogModalComponent } from './components/log-modal/log-modal.component';
import { SearchComponent } from './components/search/search.component';
import { LoginModalService } from './services/login-modal.service';
import { LoginService } from './services/login.service';

@Component({
	selector: 'app-root',
	standalone: true,
	templateUrl: './app.component.html',
	styleUrl: './app.component.css',
	imports: [RouterOutlet, HomeComponent, SearchComponent, LogModalComponent, CommonModule],
})
export class AppComponent {
	title = 'Sovereign School';
	isModalVisible: boolean = false;
	vistaMenu: boolean = false;

	constructor(
		private modalService: LoginModalService,
		public loginservice: LoginService,
		public router: Router,
	) {}

	ngOnInit() {
		this.modalService.isVisible$.subscribe((isVisible) => {
			this.isModalVisible = isVisible;
		});
	}

	openModal() {
		this.modalService.show();
	}

	salir() {
		this.vistaMenu = false;
		this.loginservice.usuario = null;
		localStorage.clear();
		this.router.navigate(['']);
	}
}
