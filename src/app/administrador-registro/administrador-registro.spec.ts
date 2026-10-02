import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdministradorRegistro } from './administrador-registro';

describe('AdministradorRegistro', () => {
  let component: AdministradorRegistro;
  let fixture: ComponentFixture<AdministradorRegistro>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdministradorRegistro],
    }).compileComponents();

    fixture = TestBed.createComponent(AdministradorRegistro);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
