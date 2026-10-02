import { TestBed } from '@angular/core/testing';

import { Administradores } from './administradores';

describe('Administradores', () => {
  let service: Administradores;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Administradores);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
