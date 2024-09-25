import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HomeChatComponent } from './home-chat.component';

describe('ChatComponent', () => {
	let component: HomeChatComponent;
	let fixture: ComponentFixture<HomeChatComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [HomeChatComponent],
		}).compileComponents();

		fixture = TestBed.createComponent(HomeChatComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
