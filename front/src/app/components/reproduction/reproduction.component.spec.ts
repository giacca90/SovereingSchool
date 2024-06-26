import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReproductionComponent } from './reproduction.component';

describe('ReproductionComponent', () => {
  let component: ReproductionComponent;
  let fixture: ComponentFixture<ReproductionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReproductionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ReproductionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
