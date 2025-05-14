import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { IntegrationService } from '../../services/integration.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  standalone: true,
  styleUrls: ['./reset-password.component.css'],
  imports: [CommonModule, ReactiveFormsModule, RouterLink]
})
export class ResetPasswordComponent {
  resetForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl('', [Validators.required])
  });

  token = '';
  message = '';
  isLoading = false;

  constructor(
    private route: ActivatedRoute,
    private integrationService: IntegrationService,
    private router: Router
  ) {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      if (!this.token) {
        this.router.navigate(['/login']);
      }
    });
  }

  onSubmit() {
    if (this.resetForm.valid &&
      this.resetForm.value.password &&
      this.resetForm.value.password === this.resetForm.value.confirmPassword &&
      this.resetForm.value.email) {
      this.isLoading = true;
      const newPassword = this.resetForm.value.password;
      const email = this.resetForm.value.email;

      this.integrationService.resetPassword(this.token, email, newPassword).subscribe({
        next: (res) => {
          this.message = 'Password reset successfully. You can now login with your new password.';
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/login']), 3000);
        },
        error: (err) => {
          this.message = 'Error resetting password. The link may have expired or email is incorrect.';
          this.isLoading = false;
          console.error(err);
        }
      });
    } else {
      this.message = 'Passwords do not match or are invalid.';
    }
  }
}
