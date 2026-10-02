import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

// Misma política que AdministradorRegistroDTO del back
const PATRON_CONTRASENA = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^\w\s]).+$/;
const NO_VACIO = /\S/;

@Component({
  selector: 'app-administrador-registro',
  imports: [ReactiveFormsModule],
  templateUrl: './administrador-registro.html',
  styleUrl: './administrador-registro.css',
})
export class AdministradorRegistro {
  readonly form = new FormGroup({
    nombre: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(NO_VACIO)],
    }),
    apellidos: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(NO_VACIO)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    contrasena: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(64),
        Validators.pattern(PATRON_CONTRASENA),
      ],
    }),
    sede: new FormControl('', { nonNullable: true }),
  });

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // La llamada al back se añade en la tarea 110
  }

  esInvalido(campo: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[campo];
    return control.invalid && (control.touched || control.dirty);
  }
}