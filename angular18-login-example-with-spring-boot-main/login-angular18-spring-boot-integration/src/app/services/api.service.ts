import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  fetchData(apiUrl: string, tableName: string): Observable<any> {
    // Validate URL format first
    if (!this.isValidUrl(apiUrl)) {
      return throwError(() => new Error('Invalid URL format - must start with http:// or https://'));
    }

    const params = new HttpParams()
      .set('apiUrl', apiUrl)  // No encoding here - let backend handle it
      .set('tableName', tableName);

    return this.http.post(`${this.baseUrl}/data/fetch`, {}, {
      params,
      headers: this.getHeaders(),
      withCredentials: true
    }).pipe(
      timeout(30000),
      catchError(error => {
        console.error('API Error Details:', error);
        let errorMsg = 'Unknown error occurred';
        if (error.status === 500) {
          errorMsg = 'Backend failed to process the external API request';
        } else if (error.status === 0) {
          errorMsg = 'Network error - could not connect to backend';
        }
        return throwError(() => new Error(errorMsg));
      })
    );
  }

  private isValidUrl(url: string): boolean {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth-key');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    });
  }
  // api.service.ts
  getAllData(tableName: string): Observable<any[]> {
    if (!tableName || tableName.trim() === '') {
      return throwError(() => new Error('Table name cannot be empty'));
    }

    const params = new HttpParams()
      .set('tableName', tableName);

    return this.http.get<any[]>(`${this.baseUrl}/data/get-all`, {
      params: params,
      headers: this.getHeaders(),
      withCredentials: true
    }).pipe(
      catchError(error => {
        console.error('GetAll Error:', error);
        let errorMsg = 'Failed to retrieve data';
        if (error.status === 404) {
          errorMsg = 'Table not found';
        } else if (error.status === 0) {
          errorMsg = 'Could not connect to server';
        }
        return throwError(() => new Error(errorMsg));
      })
    );
  }
}
