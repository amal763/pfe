import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IntegrationService } from '../../services/integration.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  emailForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });

  message = '';
  errorMessage = '';
  isLoading = false;

  constructor(
    private integrationService: IntegrationService,
    private router: Router
  ) {}

  onSubmit() {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.message = '';
    this.errorMessage = '';
    const email = this.emailForm.value.email!;

    this.integrationService.forgotPassword(email).subscribe({
      next: () => {
        this.message = 'If this email exists, reset instructions have been sent';
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Password reset error:', err);
        this.errorMessage = err.message || 'Error sending reset instructions. Please try again later.';
        this.isLoading = false;
      }
    });}

  private getErrorMessage(error: any): string {
    if (error.error?.message) {
      return error.error.message;
    }
    return 'Error sending reset instructions. Please try again later.';
  }
}
