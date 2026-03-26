import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaymentService } from '../services/payment.service';
import { Payment } from '../interfaces/payment.interface';
import { LoanService } from '../../loans/services/loan.service';
import { Loan } from '../../loans/interfaces/loan.interface';
import { CustomerService } from '../../customers/services/customer.service';
import { Customer } from '../../customers/interfaces/customer.interface';
import { UserService } from '../../users/services/user.service';
import { User } from '../../users/interfaces/user.interface';

import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { SkeletonModule } from 'primeng/skeleton';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MessageService } from 'primeng/api';
import { PaymentFormComponent } from '../components/payment-form/payment-form.component';

@Component({
  selector: 'app-payments-page',
  standalone: true,
  providers: [MessageService],
  imports: [
    FormsModule,
    TableModule,
    SelectModule,
    ButtonModule,
    ToastModule,
    SkeletonModule,
    CurrencyPipe,
    DatePipe,
    PaymentFormComponent
  ],
  templateUrl: './payments-page.component.html',
  styleUrl: './payments-page.component.css'
})
export class PaymentsPageComponent implements OnInit {
  private readonly paymentService = inject(PaymentService);
  private readonly loanService = inject(LoanService);
  private readonly customerService = inject(CustomerService);
  private readonly userService = inject(UserService);
  private readonly messageService = inject(MessageService);

  readonly payments = signal<Payment[]>([]);
  readonly loading = signal(true);
  
  readonly loans = signal<Loan[]>([]);
  readonly customers = signal<Customer[]>([]);
  readonly users = signal<User[]>([]);

  // Filter
  readonly filterLoanId = signal<number | null>(null);
  readonly filterUserId = signal<number | null>(null);
  readonly isFiltering = signal(false);

  // Form
  readonly showForm = signal(false);

  ngOnInit(): void {
    this.loadDictionaries();
    this.applyFilters();
  }

  loadDictionaries(): void {
    this.loanService.getAll().subscribe(data => this.loans.set(data));
    this.customerService.getAll().subscribe(data => this.customers.set(data));
    this.userService.getAll().subscribe(data => this.users.set(data));
  }

  applyFilters(): void {
    this.loading.set(true);
    this.isFiltering.set(true);

    const lId = this.filterLoanId();
    const uId = this.filterUserId();
    
    let request$ = this.paymentService.getAll();
    if (lId) {
      request$ = this.paymentService.getByLoan(lId);
    } else if (uId) {
      request$ = this.paymentService.getByRegisteredBy(uId);
    }

    request$.subscribe({
      next: (data) => {
        this.payments.set(data);
        this.loading.set(false);
        this.isFiltering.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.isFiltering.set(false);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los pagos.' });
      }
    });
  }

  clearFilters(): void {
    this.filterLoanId.set(null);
    this.filterUserId.set(null);
    this.applyFilters();
  }

  getLoanLabel(loanId: number): string {
    const loan = this.loans().find(l => l.id === loanId);
    if (!loan) return `Préstamo: ${loanId}`;
    const cust = this.customers().find(c => c.id === loan.customerId);
    return `${cust?.firstName || ''} ${cust?.lastName || ''} (ID: ${loanId})`;
  }

  onNew(): void {
    this.showForm.set(true);
  }

  onSaved(): void {
    this.showForm.set(false);
    this.applyFilters();
    this.messageService.add({ severity: 'success', summary: 'Éxito', detail: 'Pago registrado correctamente.' });
  }

  onCancelled(): void {
    this.showForm.set(false);
  }
}
