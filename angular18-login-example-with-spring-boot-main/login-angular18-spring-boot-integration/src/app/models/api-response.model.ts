// src/app/models/api-response.model.ts
export interface ApiResponse {
  success: boolean;
  message?: string;
  data?: any;
}

export interface TableData {
  [key: string]: any;
}
