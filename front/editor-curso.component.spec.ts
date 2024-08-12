import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditorCursoComponent } from './src/app/components/editor-curso/editor-curso.component';

describe('EditorCursoComponent', () => {
	let component: EditorCursoComponent;
	let fixture: ComponentFixture<EditorCursoComponent>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			imports: [EditorCursoComponent],
		}).compileComponents();

		fixture = TestBed.createComponent(EditorCursoComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
