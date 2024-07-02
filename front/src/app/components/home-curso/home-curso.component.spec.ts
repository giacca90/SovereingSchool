import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeCursoComponent } from './home-curso.component';

describe('HomeCursoComponent', () => {
  let component: HomeCursoComponent;
  let fixture: ComponentFixture<HomeCursoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeCursoComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(HomeCursoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
