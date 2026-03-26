import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoanApplication, LoanApplicationResolution } from '../interfaces/loan-application.interface';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LoanApplicationService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiBaseUrl}/loan-applications`;

  getAll(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(this.apiUrl);
  }

  getByCustomer(customerId: number): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  getByStatus(status: string): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.apiUrl}/status/${status}`);
  }

  getByCustomerAndStatus(customerId: number, status: string): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.apiUrl}/customer/${customerId}/status/${status}`);
  }

  getByEvaluator(evaluatorId: number): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.apiUrl}/evaluator/${evaluatorId}`);
  }

  create(body: Omit<LoanApplication, 'id' | 'status' | 'applicationDate' | 'resolutionDetails' | 'evaluatedBy'>): Observable<LoanApplication> {
    return this.http.post<LoanApplication>(this.apiUrl, body);
  }

  update(id: number, body: Omit<LoanApplication, 'id'>): Observable<LoanApplication> {
    return this.http.put<LoanApplication>(`${this.apiUrl}/${id}`, body);
  }

  assignEvaluator(id: number, evaluatorId: number): Observable<LoanApplication> {
    return this.http.put<LoanApplication>(`${this.apiUrl}/${id}/assign-evaluator/${evaluatorId}`, {});
  }

  resolve(id: number, dto: LoanApplicationResolution): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/resolve`, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
