import { Component } from '@angular/core';
import { LoginModalServiceService } from '../../../services/login-modal-service.service';

@Component({
	selector: 'app-login',
	standalone: true,
	imports: [],
	templateUrl: './login.component.html',
	styleUrl: './login.component.css',
})
export class LoginComponent {
	constructor(private modalService: LoginModalServiceService) {}

	close() {
		this.modalService.hide();
	}
}
