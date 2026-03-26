import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Customer } from '../interfaces/customer.interface';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiBaseUrl}/customers`;

  /** Retrieves all customers from the backend. */
  getAll(): Observable<Customer[]> {
    return this.http.get<Customer[]>(this.apiUrl);
  }

  /** Creates a new customer. */
  create(body: Omit<Customer, 'id'>): Observable<Customer> {
    return this.http.post<Customer>(this.apiUrl, body);
  }

  /** Updates an existing customer. */
  update(id: number, body: Omit<Customer, 'id'>): Observable<Customer> {
    return this.http.put<Customer>(`${this.apiUrl}/${id}`, body);
  }

  /** Deletes a customer by ID. */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
