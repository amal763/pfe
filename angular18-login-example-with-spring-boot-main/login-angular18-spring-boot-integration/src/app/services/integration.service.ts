import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Injectable } from '@angular/core';
import {catchError, Observable} from 'rxjs';
import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { SignupRequest } from '../models/signup-request';
import { SignupResponse } from '../models/signup-response';

const BASE_URL = "http://localhost:8081/api";

@Injectable({ providedIn: 'root' })
export class IntegrationService {
  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-key');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    });
  }

  doLogin(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${BASE_URL}/doLogin`,
      request,
      { headers: this.getHeaders() }
    );
  }

  dashboard(): Observable<any> {
    return this.http.get<any>(
      `${BASE_URL}/dashboard`,
      { headers: this.getHeaders() }
    );
  }

  doRegister(request: SignupRequest): Observable<SignupResponse> {
    return this.http.post<SignupResponse>(`${BASE_URL}/doRegister`, request);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${BASE_URL}/forgot-password`, { email }).pipe(
      catchError(error => {
        // Handle different error scenarios
        if (error.status === 404) {
          throw new Error('Email not found');
        } else if (error.status === 500) {
          throw new Error('Server error. Please try again later.');
        } else {
          throw new Error('Failed to send reset instructions');
        }
      })
    );
  }

  resetPassword(token: string, email: string, newPassword: string): Observable<any> {
    return this.http.post(`${BASE_URL}/reset-password`, {
      token,
      email,
      newPassword
    });
  }
}
