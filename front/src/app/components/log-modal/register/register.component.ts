import { Component } from '@angular/core';
import { LoginModalServiceService } from '../../../services/login-modal-service.service';

@Component({
	selector: 'app-register',
	standalone: true,
	imports: [],
	templateUrl: './register.component.html',
	styleUrl: './register.component.css',
})
export class RegisterComponent {
	constructor(private modalService: LoginModalServiceService) {}

	close() {
		this.modalService.hide();
	}
}
