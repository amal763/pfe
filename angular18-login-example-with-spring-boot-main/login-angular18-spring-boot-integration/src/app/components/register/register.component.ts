import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { IntegrationService } from '../../services/integration.service';
import { LocalStorageService } from '../../services/local-storage.service';
import { SignupRequest } from '../../models/signup-request';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  constructor(
    private integrationService: IntegrationService,
    private storage: LocalStorageService
  ) { }

  request: SignupRequest = new SignupRequest();
  msg: string | undefined;
  isLoading = false;

  signupForm: FormGroup = new FormGroup({
    name: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    address: new FormControl('', Validators.required),
    mobileno: new FormControl('', Validators.required),
    age: new FormControl('', Validators.required)
  });

  public onSubmit() {
    if (this.signupForm.invalid) {
      this.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.storage.remove('auth-key');

    const formValue = this.signupForm.value;

    this.request = {
      name: formValue.name,
      email: formValue.email,
      password: formValue.password,
      address: formValue.address,
      mobileNo: formValue.mobileno, // Note the property name change
      age: formValue.age
    };

    this.integrationService.doRegister(this.request).subscribe({
      next: (res) => {
        this.msg = res.response;
        this.isLoading = false;
      },
      error: (err) => {
        console.error("Registration error:", err);
        this.msg = err.error?.message || 'Registration failed';
        this.isLoading = false;
      }
    });
  }

  private markAllAsTouched() {
    Object.values(this.signupForm.controls).forEach(control => {
      control.markAsTouched();
    });
  }
}
