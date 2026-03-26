import { Component, inject, input, output, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Customer } from '../../interfaces/customer.interface';
import { CustomerService } from '../../services/customer.service';

import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';
import { TextareaModule } from 'primeng/textarea';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { PhoneMaskDirective } from '../../../shared/directives/phone-mask.directive';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DialogModule,
    InputTextModule,
    DatePickerModule,
    TextareaModule,
    ButtonModule,
    MessageModule,
    PhoneMaskDirective
  ],
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.css'
})
export class CustomerFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly customerService = inject(CustomerService);

  /** When provided, the modal operates in edit mode. */
  readonly customer = input<Customer | null>(null);
  /** Emits when the form is submitted successfully. */
  readonly saved = output<Customer>();
  /** Emits when the dialog is closed without saving. */
  readonly cancelled = output<void>();

  visible = signal(true);
  loading = signal(false);
  errorMessage = signal<string | undefined>(undefined);

  form!: FormGroup;

  get isEditMode(): boolean {
    return !!this.customer();
  }

  readonly today = new Date();

  ngOnInit(): void {
    const c = this.customer();
    this.form = this.fb.group({
      firstName:   [c?.firstName   ?? '', [Validators.required, Validators.maxLength(100)]],
      lastName:    [c?.lastName    ?? '', [Validators.required, Validators.maxLength(100)]],
      nit:         [c?.nit         ?? '', [Validators.required, Validators.minLength(8), Validators.maxLength(9)]],
      dateOfBirth: [c?.dateOfBirth ? new Date(c.dateOfBirth) : null, [Validators.required]],
      address:     [c?.address     ?? '', [Validators.maxLength(255)]],
      email:       [c?.email       ?? '', [Validators.email, Validators.maxLength(100)]],
      phoneNumber: [c?.phoneNumber ?? '', [Validators.maxLength(20)]]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(undefined);

    const raw = this.form.getRawValue();
    const payload = {
      ...raw,
      dateOfBirth: (raw.dateOfBirth as Date).toISOString().split('T')[0]
    };

    const request$ = this.isEditMode
      ? this.customerService.update(this.customer()!.id, payload)
      : this.customerService.create(payload);

    request$.subscribe({
      next: (saved) => {
        this.loading.set(false);
        this.saved.emit(saved);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Ocurrió un error al guardar el cliente. Intenta de nuevo.');
      }
    });
  }

  onClose(): void {
    this.cancelled.emit();
  }

  /** Returns true when control is invalid and has been touched. */
  isInvalid(name: string): boolean {
    const ctrl = this.form.get(name);
    return !!(ctrl?.invalid && ctrl.touched);
  }
}
