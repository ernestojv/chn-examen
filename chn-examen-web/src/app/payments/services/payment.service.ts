import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Payment } from '../interfaces/payment.interface';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiBaseUrl}/payments`;

  getAll(): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.apiUrl);
  }

  getById(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.apiUrl}/${id}`);
  }

  getByLoan(loanId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/loan/${loanId}`);
  }

  getByRegisteredBy(userId: number): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.apiUrl}/registered-by/${userId}`);
  }

  create(body: Omit<Payment, 'id' | 'paymentDate'>): Observable<Payment> {
    return this.http.post<Payment>(this.apiUrl, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
