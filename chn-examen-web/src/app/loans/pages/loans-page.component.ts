import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoanService } from '../services/loan.service';
import { Loan } from '../interfaces/loan.interface';
import { CustomerService } from '../../customers/services/customer.service';
import { Customer } from '../../customers/interfaces/customer.interface';

import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { SkeletonModule } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { CurrencyPipe } from '@angular/common';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-loans-page',
  standalone: true,
  providers: [MessageService],
  imports: [
    FormsModule,
    TableModule,
    SelectModule,
    ButtonModule,
    ToastModule,
    SkeletonModule,
    TagModule,
    DialogModule,
    CurrencyPipe
  ],
  templateUrl: './loans-page.component.html',
  styleUrl: './loans-page.component.css'
})
export class LoansPageComponent implements OnInit {
  private readonly loanService = inject(LoanService);
  private readonly customerService = inject(CustomerService);
  private readonly messageService = inject(MessageService);

  readonly loans = signal<Loan[]>([]);
  readonly loading = signal(true);
  readonly customers = signal<Customer[]>([]);

  // Filters
  readonly filterCustomerId = signal<number | null>(null);
  readonly isFiltering = signal(false);

  // Details Modal
  readonly selectedLoan = signal<Loan | null>(null);
  readonly showDetailsDialog = signal(false);

  ngOnInit(): void {
    this.customerService.getAll().subscribe(data => this.customers.set(data));
    this.applyFilters();
  }

  applyFilters(): void {
    this.loading.set(true);
    this.isFiltering.set(true);

    const cId = this.filterCustomerId();
    const request$ = cId 
      ? this.loanService.getByCustomer(cId)
      : this.loanService.getAll();

    request$.subscribe({
      next: (data) => {
        this.loans.set(data);
        this.loading.set(false);
        this.isFiltering.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.isFiltering.set(false);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los préstamos.' });
      }
    });
  }

  clearFilters(): void {
    this.filterCustomerId.set(null);
    this.applyFilters();
  }

  getCustomerName(id: number): string {
    const c = this.customers().find(x => x.id === id);
    return c ? `${c.firstName} ${c.lastName}` : `ID: ${id}`;
  }

  getStatusSeverity(status: string | undefined): 'success' | 'warn' | 'danger' | 'info' {
    const normalized = status?.toUpperCase();

    switch (normalized) {
      case 'PAID': return 'success';
      case 'CURRENT': return 'info';
      case 'UP_TO_DATE': return 'info';
      case 'OVERDUE': return 'danger';
      case 'PENDING': return 'warn';
      default: return 'info';
    }
  }

  getStatusLabel(status: string | undefined): string {
    const normalized = status?.toUpperCase();

    switch (normalized) {
      case 'PAID': return 'Pagado';
      case 'CURRENT': return 'Al día';
      case 'UP_TO_DATE': return 'Al día';
      case 'OVERDUE': return 'Mora';
      case 'PENDING': return 'Pendiente';
      default: return status || 'Activo';
    }
  }

  onViewDetails(loan: Loan): void {
    this.selectedLoan.set(loan);
    this.showDetailsDialog.set(true);
  }

  getCustomer(id: number): Customer | undefined {
    return this.customers().find(x => x.id === id);
  }
}

