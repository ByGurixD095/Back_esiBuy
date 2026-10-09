import { Component, input, output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

export type MfaAuthMode = 'login' | 'setup' | 'verify' | 'backup';

@Component({
  selector: 'app-mfa-auth',
  imports: [ReactiveFormsModule],
  templateUrl: './mfa-auth.html',
  styleUrl: './mfa-auth.css',
})
export class MfaAuth {
  readonly mode = input<MfaAuthMode>('verify');
  readonly qrCodeUri = input('');
  readonly manualSecretKey = input('');
  readonly requiereEmailOtp = input(false);
  readonly backupCodes = input<string[]>([]);
  readonly errorMessage = input('');

  readonly confirmarConfiguracion = output<string>();
  readonly verificar = output<{ totpCode: string; emailOtpCode: string }>();
  readonly finalizarRespaldo = output<void>();

  readonly setupForm = new FormGroup({
    code: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  readonly verifyForm = new FormGroup({
    totpCode: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    emailOtpCode: new FormControl('', { nonNullable: true }),
  });

  enviarConfiguracion(): void {
    if (this.setupForm.invalid) {
      this.setupForm.markAllAsTouched();
      return;
    }
    this.confirmarConfiguracion.emit(this.setupForm.controls.code.value.trim());
  }

  enviarVerificacion(): void {
    if (this.verifyForm.invalid) {
      this.verifyForm.markAllAsTouched();
      return;
    }
    const { totpCode, emailOtpCode } = this.verifyForm.getRawValue();
    this.verificar.emit({ totpCode: totpCode.trim(), emailOtpCode: emailOtpCode.trim() });
  }
}
