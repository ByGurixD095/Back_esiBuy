import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdministradorRegistro } from './administrador-registro';

describe('AdministradorRegistro', () => {
  let component: AdministradorRegistro;
  let fixture: ComponentFixture<AdministradorRegistro>;

  const datosValidos = {
    nombre: 'Ana',
    apellidos: 'López García',
    email: 'ana.lopez@esi.es',
    contrasena: 'Admin123!x',
    sede: 'Ciudad Real',
  };

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

  it('el formulario vacío es inválido', () => {
    expect(component.form.valid).toBe(false);
  });

  it('con datos válidos el formulario es válido', () => {
    component.form.setValue(datosValidos);
    expect(component.form.valid).toBe(true);
  });

  it('la sede es opcional', () => {
    component.form.setValue({ ...datosValidos, sede: '' });
    expect(component.form.valid).toBe(true);
  });

  it('rechaza un email con formato inválido', () => {
    component.form.setValue({ ...datosValidos, email: 'ana.lopez' });
    expect(component.form.controls.email.valid).toBe(false);
  });

  it('rechaza una contraseña demasiado corta', () => {
    component.form.setValue({ ...datosValidos, contrasena: 'Ab1!' });
    expect(component.form.controls.contrasena.valid).toBe(false);
  });

  it('rechaza una contraseña sin mayúscula, número o símbolo', () => {
    for (const debil of ['abcdefgh', 'ABCDEFGH', 'Abcdefgh', 'Abcdefg1', 'abcdefg1!']) {
      component.form.setValue({ ...datosValidos, contrasena: debil });
      expect(component.form.controls.contrasena.valid).toBe(false);
    }
  });

  it('muestra los cinco campos del formulario', () => {
    const el = fixture.nativeElement as HTMLElement;
    for (const campo of ['nombre', 'apellidos', 'email', 'contrasena', 'sede']) {
      expect(el.querySelector(`[formControlName="${campo}"]`)).not.toBeNull();
    }
  });

  it('el botón de enviar está deshabilitado mientras el formulario sea inválido', () => {
    const boton = (fixture.nativeElement as HTMLElement).querySelector(
      'button[type="submit"]',
    ) as HTMLButtonElement;
    expect(boton.disabled).toBe(true);
  });
});