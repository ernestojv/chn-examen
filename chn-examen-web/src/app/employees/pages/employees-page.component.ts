import { Component, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EmployeeService } from '../services/employee.service';
import { Employee } from '../interfaces/employee.interface';
import { EmployeeFormComponent } from '../components/employee-form/employee-form.component';

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
  selector: 'app-employees-page',
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
    EmployeeFormComponent
  ],
  templateUrl: './employees-page.component.html',
  styleUrl: './employees-page.component.css'
})
export class EmployeesPageComponent {
  private readonly employeeService = inject(EmployeeService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  readonly employees = signal<Employee[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');

  readonly formEmployee = signal<Employee | null | undefined>(undefined);
  readonly showForm = signal(false);

  readonly filteredEmployees = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.employees();
    return this.employees().filter(e =>
      `${e.firstName} ${e.lastName}`.toLowerCase().includes(query) ||
      e.employeeCode.toLowerCase().includes(query) ||
      (e.position ?? '').toLowerCase().includes(query)
    );
  });

  constructor() {
    this.loadEmployees();
  }

  private loadEmployees(): void {
    this.loading.set(true);
    this.employeeService.getAll().subscribe({
      next: (data) => {
        this.employees.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onNew(): void {
    this.formEmployee.set(null);
    this.showForm.set(true);
  }

  onEdit(employee: Employee): void {
    this.formEmployee.set(employee);
    this.showForm.set(true);
  }

  onDelete(employee: Employee): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que deseas eliminar a <strong>${employee.firstName} ${employee.lastName}</strong>? Esta acción no se puede deshacer.`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      accept: () => {
        this.employeeService.delete(employee.id).subscribe({
          next: () => {
            this.employees.update(list => list.filter(e => e.id !== employee.id));
            this.messageService.add({
              severity: 'success',
              summary: 'Empleado eliminado',
              detail: `${employee.firstName} ${employee.lastName}`,
              life: 3000
            });
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo eliminar el empleado.',
              life: 4000
            });
          }
        });
      }
    });
  }

  onSaved(saved: Employee): void {
    const isEdit = !!this.formEmployee();
    if (isEdit) {
      this.employees.update(list => list.map(e => e.id === saved.id ? saved : e));
    } else {
      this.employees.update(list => [...list, saved]);
    }
    this.showForm.set(false);
    this.messageService.add({
      severity: 'success',
      summary: isEdit ? 'Empleado actualizado' : 'Empleado creado',
      detail: `${saved.firstName} ${saved.lastName}`,
      life: 3000
    });
  }

  onCancelled(): void {
    this.showForm.set(false);
  }
}
