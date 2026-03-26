import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LoanApplicationService } from '../services/loan-application.service';
import { LoanApplication } from '../interfaces/loan-application.interface';
import { CustomerService } from '../../customers/services/customer.service';
import { Customer } from '../../customers/interfaces/customer.interface';
import { EmployeeService } from '../../employees/services/employee.service';
import { Employee } from '../../employees/interfaces/employee.interface';
import { LoanApplicationFormComponent } from '../components/loan-application-form/loan-application-form.component';

import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { SkeletonModule } from 'primeng/skeleton';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TagModule } from 'primeng/tag';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-loan-applications-page',
  standalone: true,
  providers: [MessageService, ConfirmationService],
  imports: [
    FormsModule,
    TableModule,
    SelectModule,
    ButtonModule,
    ToastModule,
    SkeletonModule,
    ConfirmDialogModule,
    TagModule,
    CurrencyPipe,
    DatePipe,
    LoanApplicationFormComponent
  ],
  templateUrl: './loan-applications-page.component.html',
  styleUrl: './loan-applications-page.component.css'
})
export class LoanApplicationsPageComponent implements OnInit {
  private readonly loanService = inject(LoanApplicationService);
  private readonly customerService = inject(CustomerService);
  private readonly employeeService = inject(EmployeeService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  readonly applications = signal<LoanApplication[]>([]);
  readonly loading = signal(true);
  
  readonly customers = signal<Customer[]>([]);
  readonly employees = signal<Employee[]>([]);

  // Filters
  readonly filterCustomerId = signal<number | null>(null);
  readonly filterStatus = signal<string | null>(null);
  readonly filterEvaluatorId = signal<number | null>(null);
  readonly isFiltering = signal(false);

  readonly statusOptions = [
    { label: 'Pendiente', value: 'PENDING' },
    { label: 'Aprobado', value: 'APPROVED' },
    { label: 'Rechazado', value: 'REJECTED' }
  ];

  readonly formApplication = signal<LoanApplication | null | undefined>(undefined);
  readonly showForm = signal(false);

  ngOnInit(): void {
    this.loadDictionaries();
    this.applyFilters();
  }

  private loadDictionaries(): void {
    this.customerService.getAll().subscribe(data => this.customers.set(data));
    this.employeeService.getAll().subscribe(data => this.employees.set(data));
  }

  applyFilters(): void {
    this.loading.set(true);
    this.isFiltering.set(true);

    const cId = this.filterCustomerId();
    const status = this.filterStatus();
    const eId = this.filterEvaluatorId();

    let request$ = this.loanService.getAll();

    // Determine the most specific backend endpoint to use
    if (cId && status) {
      request$ = this.loanService.getByCustomerAndStatus(cId, status);
    } else if (cId) {
      request$ = this.loanService.getByCustomer(cId);
    } else if (status) {
      request$ = this.loanService.getByStatus(status);
    } else if (eId) {
      request$ = this.loanService.getByEvaluator(eId);
    }

    request$.subscribe({
      next: (data) => {
        // If we fetched using an endpoint that doesn't cover all filters, apply client-side filtering for the rest.
        let filtered = data;
        
        // If we used the evaluator endpoint, we still need to filter by customer & status if they were set (though our if-else didn't allow that combination to hit this branch, but just in case of future logic changes)
        // If we used the customer endpoint, and evaluator is set, filter locally.
        if (eId) {
          filtered = filtered.filter(a => a.evaluatedBy === eId);
        }
        if (status && cId && eId) {
           // We fetched by CustomerAndStatus, just need to filter evaluator locally.
           filtered = filtered.filter(a => a.evaluatedBy === eId);
        }

        this.applications.set(filtered);
        this.loading.set(false);
        this.isFiltering.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.isFiltering.set(false);
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar las solicitudes.' });
      }
    });
  }

  clearFilters(): void {
    this.filterCustomerId.set(null);
    this.filterStatus.set(null);
    this.filterEvaluatorId.set(null);
    this.applyFilters();
  }

  getCustomerName(id: number): string {
    const c = this.customers().find(x => x.id === id);
    return c ? `${c.firstName} ${c.lastName}` : `ID: ${id}`;
  }

  getEvaluatorName(id: number | undefined): string {
    if (!id) return 'Sin asignar';
    const e = this.employees().find(x => x.id === id);
    return e ? `${e.firstName} ${e.lastName}` : `ID: ${id}`;
  }

  getStatusSeverity(status: string | undefined): 'success' | 'warn' | 'danger' | 'info' {
    switch (status) {
      case 'APPROVED': return 'success';
      case 'PENDING': return 'warn';
      case 'REJECTED': return 'danger';
      default: return 'info';
    }
  }

  getStatusLabel(status: string | undefined): string {
    switch (status) {
      case 'APPROVED': return 'Aprobado';
      case 'PENDING': return 'Pendiente';
      case 'REJECTED': return 'Rechazado';
      default: return status || 'Desconocido';
    }
  }

  onNew(): void {
    this.formApplication.set(null);
    this.showForm.set(true);
  }

  onEdit(app: LoanApplication): void {
    this.formApplication.set(app);
    this.showForm.set(true);
  }

  onDelete(app: LoanApplication): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que deseas eliminar la solicitud por <strong>${app.requestedAmount}</strong> del cliente? Esta acción no se puede deshacer.`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      accept: () => {
        this.loanService.delete(app.id).subscribe({
          next: () => {
            this.applications.update(list => list.filter(a => a.id !== app.id));
            this.messageService.add({ severity: 'success', summary: 'Eliminada', detail: 'Solicitud eliminada.', life: 3000 });
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error?.message || 'No se pudo eliminar la solicitud.', life: 4000 });
          }
        });
      }
    });
  }

  onSaved(saved: LoanApplication): void {
    // Re-fetch to apply current filters correctly
    this.applyFilters();
    this.showForm.set(false);
    this.messageService.add({
      severity: 'success',
      summary: 'Éxito',
      detail: 'Solicitud guardada correctamente.',
      life: 3000
    });
  }

  onCancelled(): void {
    this.showForm.set(false);
  }
}

