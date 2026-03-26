import { Component, inject, input, output, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Employee } from '../../interfaces/employee.interface';
import { EmployeeService } from '../../services/employee.service';

import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DialogModule,
    InputTextModule,
    ButtonModule,
    MessageModule
  ],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.css'
})
export class EmployeeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly employeeService = inject(EmployeeService);

  readonly employee = input<Employee | null>(null);
  readonly saved = output<Employee>();
  readonly cancelled = output<void>();

  visible = signal(true);
  loading = signal(false);
  errorMessage = signal<string | undefined>(undefined);
  form!: FormGroup;

  get isEditMode(): boolean {
    return !!this.employee();
  }

  ngOnInit(): void {
    const e = this.employee();
    this.form = this.fb.group({
      employeeCode: [e?.employeeCode ?? '', [Validators.required, Validators.maxLength(50)]],
      firstName:    [e?.firstName    ?? '', [Validators.required, Validators.maxLength(100)]],
      lastName:     [e?.lastName     ?? '', [Validators.required, Validators.maxLength(100)]],
      position:     [e?.position     ?? '', [Validators.maxLength(100)]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(undefined);

    const payload = this.form.getRawValue();

    const request$ = this.isEditMode
      ? this.employeeService.update(this.employee()!.id, payload)
      : this.employeeService.create(payload);

    request$.subscribe({
      next: (saved) => {
        this.loading.set(false);
        this.saved.emit(saved);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Ocurrió un error al guardar el empleado. Verifica los datos.');
      }
    });
  }

  onClose(): void {
    this.cancelled.emit();
  }

  isInvalid(name: string): boolean {
    const ctrl = this.form.get(name);
    return !!(ctrl?.invalid && ctrl.touched);
  }
}
