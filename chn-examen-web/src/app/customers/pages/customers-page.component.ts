import { Component, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CustomerService } from '../services/customer.service';
import { Customer } from '../interfaces/customer.interface';
import { CustomerFormComponent } from '../components/customer-form/customer-form.component';

import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { ToastModule } from 'primeng/toast';
import { SkeletonModule } from 'primeng/skeleton';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-customers-page',
  standalone: true,
  providers: [MessageService, ConfirmationService],
  imports: [
    FormsModule,
    TableModule,
    InputTextModule,
    ButtonModule,
    IconFieldModule,
    InputIconModule,
    ToastModule,
    SkeletonModule,
    ConfirmDialogModule,
    CustomerFormComponent
  ],
  templateUrl: './customers-page.component.html',
  styleUrl: './customers-page.component.css'
})
export class CustomersPageComponent {
  private readonly customerService = inject(CustomerService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  readonly customers = signal<Customer[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');
  readonly formCustomer = signal<Customer | null | undefined>(undefined);
  readonly showForm = signal(false);

  readonly filteredCustomers = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.customers();
    return this.customers().filter(c =>
      `${c.firstName} ${c.lastName}`.toLowerCase().includes(query) ||
      c.nit.toLowerCase().includes(query) ||
      (c.email ?? '').toLowerCase().includes(query)
    );
  });

  constructor() {
    this.loadCustomers();
  }

  private loadCustomers(): void {
    this.loading.set(true);
    this.customerService.getAll().subscribe({
      next: (data) => {
        this.customers.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onNew(): void {
    this.formCustomer.set(null);
    this.showForm.set(true);
  }

  onEdit(customer: Customer): void {
    this.formCustomer.set(customer);
    this.showForm.set(true);
  }

  onDelete(customer: Customer): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que deseas eliminar a <strong>${customer.firstName} ${customer.lastName}</strong>? Esta acción no se puede deshacer.`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      accept: () => {
        this.customerService.delete(customer.id).subscribe({
          next: () => {
            this.customers.update(list => list.filter(c => c.id !== customer.id));
            this.messageService.add({
              severity: 'success',
              summary: 'Cliente eliminado',
              detail: `${customer.firstName} ${customer.lastName}`,
              life: 3000
            });
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo eliminar el cliente.',
              life: 4000
            });
          }
        });
      }
    });
  }

  onSaved(saved: Customer): void {
    const isEdit = !!this.formCustomer();
    if (isEdit) {
      this.customers.update(list => list.map(c => c.id === saved.id ? saved : c));
    } else {
      this.customers.update(list => [...list, saved]);
    }
    this.showForm.set(false);
    this.messageService.add({
      severity: 'success',
      summary: isEdit ? 'Cliente actualizado' : 'Cliente creado',
      detail: `${saved.firstName} ${saved.lastName}`,
      life: 3000
    });
  }

  onCancelled(): void {
    this.showForm.set(false);
  }
}
