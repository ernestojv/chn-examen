import { Component, inject, input, output, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { User } from '../../interfaces/user.interface';
import { UserService } from '../../services/user.service';
import { EmployeeService } from '../../../employees/services/employee.service';
import { Employee } from '../../../employees/interfaces/employee.interface';

import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DialogModule,
    InputTextModule,
    PasswordModule,
    SelectModule,
    ButtonModule,
    MessageModule
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css'
})
export class UserFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly employeeService = inject(EmployeeService);

  readonly user = input<User | null>(null);
  readonly saved = output<User>();
  readonly cancelled = output<void>();

  visible = signal(true);
  loading = signal(false);
  errorMessage = signal<string | undefined>(undefined);
  form!: FormGroup;
  
  readonly employees = signal<Employee[]>([]);
  readonly loadingEmployees = signal(true);

  get isEditMode(): boolean {
    return !!this.user();
  }

  ngOnInit(): void {
    const u = this.user();
    this.form = this.fb.group({
      username:   [u?.username ?? '', [Validators.required, Validators.maxLength(50)]],
      password:   ['', this.isEditMode ? [] : [Validators.required, Validators.maxLength(255)]],
      employeeId: [u?.employeeId ?? null, [Validators.required]]
    });

    this.loadEmployees();
  }

  private loadEmployees(): void {
    this.employeeService.getAll().subscribe({
      next: (data) => {
        this.employees.set(data);
        this.loadingEmployees.set(false);
      },
      error: () => this.loadingEmployees.set(false)
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
    // If editing and password is empty, don't send it
    if (this.isEditMode && !payload.password) {
      delete payload.password;
    }

    const request$ = this.isEditMode
      ? this.userService.patch(this.user()!.id, payload)
      : this.userService.create(payload);

    request$.subscribe({
      next: (saved) => {
        this.loading.set(false);
        this.saved.emit(saved);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Ocurrió un error al guardar el usuario. Verifica los datos.');
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
