import { Component } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { FormsModule } from '@angular/forms';
import { NgClass, NgForOf, NgIf } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  standalone: true,
  imports: [FormsModule, NgForOf, NgIf, NgClass],
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  // Form fields
  apiUrl: string = '';
  tableName: string = '';

  // Data display properties
  tableData: any[] = [];
  filteredData: any[] = [];
  tableColumns: string[] = [];
  showTable: boolean = false;

  // Loading and error states
  isLoading: boolean = false;
  errorMessage: string | null = null;

  // Filtering and sorting properties
  globalFilter: string = '';
  columnFilters: { [key: string]: string } = {};
  sortColumnName: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';

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

  getAllData() {
    if (!this.tableName || this.tableName.trim() === '') {
      this.errorMessage = 'Please enter a valid table name';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.showTable = false;
    this.resetFilters(); // Reset filters when fetching new data

    this.apiService.getAllData(this.tableName).subscribe({
      next: (data: any[]) => {
        if (data && data.length > 0) {
          this.tableColumns = this.extractColumns(data);
          this.tableData = data;
          this.filteredData = [...data]; // Initialize filtered data
          this.showTable = true;
        } else {
          this.errorMessage = 'No data available in this table';
        }
        this.isLoading = false;
      },
      error: (error: Error) => {
        console.error('Data retrieval failed:', error);
        this.errorMessage = error.message;
        this.isLoading = false;
      }
    });
  }

  private extractColumns(data: any[]): string[] {
    const firstItemWithData = data.find(item => item && Object.keys(item).length > 0);
    return firstItemWithData ? Object.keys(firstItemWithData) : [];
  }

  // Filtering methods
  get filterableColumns(): string[] {
    return this.tableColumns || [];
  }

  applyFilter(): void {
    this.filteredData = [...this.tableData];

    // Apply global filter
    if (this.globalFilter) {
      const searchTerm = this.globalFilter.toLowerCase();
      this.filteredData = this.filteredData.filter(row =>
        Object.values(row).some(val =>
          String(val).toLowerCase().includes(searchTerm)
        )
      );
    }

    // Apply column filters
    for (const [column, filterValue] of Object.entries(this.columnFilters)) {
      if (filterValue) {
        const searchTerm = filterValue.toLowerCase();
        this.filteredData = this.filteredData.filter(row =>
          String(row[column]).toLowerCase().includes(searchTerm)
        );
      }
    }

    // Apply sorting
    if (this.sortColumnName) {
      this.filteredData.sort((a, b) => {
        const valA = a[this.sortColumnName];
        const valB = b[this.sortColumnName];

        if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
        if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
        return 0;
      });
    }
  }

  // Sorting methods
  sortColumn(column: string): void {
    if (this.sortColumnName === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumnName = column;
      this.sortDirection = 'asc';
    }
    this.applyFilter();
  }

  isSortable(column: string): boolean {
    // You can add logic here to make only certain columns sortable
    return true;
  }

  // Utility methods
  resetFilters(): void {
    this.globalFilter = '';
    this.columnFilters = {};
    this.sortColumnName = '';
    this.sortDirection = 'asc';
    this.filteredData = [...this.tableData];
  }

  isStatusColumn(column: string): boolean {
    // Check if column contains status values (case insensitive)
    return column.toLowerCase().includes('status');
  }
  onUpdate(row: any): void {
    console.log('Update clicked for:', row);
    // Implement your update logic here
  }

}
