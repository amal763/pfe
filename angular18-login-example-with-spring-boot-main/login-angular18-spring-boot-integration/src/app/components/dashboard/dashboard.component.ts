import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { FormsModule } from '@angular/forms';
import {NgClass, NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  standalone: true,
  imports: [FormsModule, NgForOf, NgIf, NgClass],
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  apiUrl: string = '';
  tableName: string = '';
  tableData: any[] = [];
  tableColumns: string[] = [];
  showTable: boolean = false;
  isLoading: boolean = false;
  errorMessage: string | null = null;

  constructor(private apiService: ApiService) {}

  fetchData() {
    console.log('Form submitted with:', {
      apiUrl: this.apiUrl,
      tableName: this.tableName
    });

    if (!this.apiUrl || !this.tableName) {
      this.errorMessage = 'Veuillez entrer une URL API valide et un nom de table';
      console.error('Validation failed:', this.errorMessage);
      return;
    }

    console.log('Attempting to fetch from:', this.apiUrl);
    this.isLoading = true;
    this.errorMessage = null;
    this.showTable = false;

    this.apiService.fetchData(this.apiUrl, this.tableName).subscribe({
      next: (response) => {
        console.log('Fetch successful', response);
        this.getAllData();
      },
      error: (error) => {
        console.error('Fetch error:', error);
        this.isLoading = false;

        if (error.name === 'TimeoutError') {
          this.errorMessage = 'Connexion expirée: Le serveur ne répond pas après 30 secondes';
        } else if (error.status === 0) {
          this.errorMessage = 'Impossible de se connecter au serveur - vérifiez votre connexion';
        } else if (error.status === 404) {
          this.errorMessage = 'URL non trouvée - vérifiez l\'adresse de l\'API';
        } else {
          this.errorMessage = `Erreur: ${error.message || 'Erreur inconnue'}`;
        }
      },
      complete: () => {
        console.log('Fetch operation completed');
        this.isLoading = false;
      }
    });
  }

// dashboard.component.ts
  getAllData() {
    if (!this.tableName || this.tableName.trim() === '') {
      this.errorMessage = 'Please enter a valid table name';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.showTable = false;

    this.apiService.getAllData(this.tableName).subscribe({
      next: (data: any[]) => {
        if (data && data.length > 0) {
          // Safely extract columns even if empty objects
          this.tableColumns = this.extractColumns(data);
          this.tableData = data;
          this.showTable = true;
        } else {
          this.errorMessage = 'No data available in this table';
        }
        this.isLoading = false;
      },
      error: (error: Error) => {  // Using Error type instead of any
        console.error('Data retrieval failed:', error);
        this.errorMessage = error.message;
        this.isLoading = false;
      }
    });
  }

  private extractColumns(data: any[]): string[] {
    // Handle case where first item might be empty
    const firstItemWithData = data.find(item => item && Object.keys(item).length > 0);
    return firstItemWithData ? Object.keys(firstItemWithData) : [];
  }
}
