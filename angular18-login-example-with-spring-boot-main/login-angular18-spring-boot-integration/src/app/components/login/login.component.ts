import { Component, inject } from '@angular/core';
import { IntegrationService } from '../../services/integration.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoginRequest } from '../../models/login-request';
import { Router, RouterLink } from '@angular/router';
import { LocalStorageService } from '../../services/local-storage.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  constructor(
    private integration: IntegrationService,
    private storage: LocalStorageService
  ) {}

  userForm: FormGroup = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)])
  });

  router = inject(Router);
  request: LoginRequest = { email: '', password: '' };
  errorMessage: string | null = null;
  isLoading = false;

  login() {
    // Reset previous errors
    this.errorMessage = null;

    // Mark all fields as touched to show validation errors
    this.userForm.markAllAsTouched();

    if (this.userForm.invalid) {
      this.errorMessage = 'Please fill in all fields correctly';
      return;
    }

    this.isLoading = true;
    this.storage.remove('auth-key');

    this.request = {
      email: this.userForm.value.email,
      password: this.userForm.value.password
    };

    this.integration.doLogin(this.request).subscribe({
      next: (res) => {
        this.storage.set('auth-key', res.token);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error("Login error:", err);
        this.errorMessage = 'Invalid email or password';
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }
}
