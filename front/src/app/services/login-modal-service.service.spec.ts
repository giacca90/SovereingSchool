import { TestBed } from '@angular/core/testing';

import { LoginModalServiceService } from './login-modal-service.service';

describe('LoginModalServiceService', () => {
  let service: LoginModalServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoginModalServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
