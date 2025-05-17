import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { profGuard } from './prof.guard';

describe('profGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => profGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
