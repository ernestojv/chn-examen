import { Component, inject, input, output, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoanApplication, LoanApplicationResolution } from '../../interfaces/loan-application.interface';
import { LoanApplicationService } from '../../services/loan-application.service';
import { Customer } from '../../../customers/interfaces/customer.interface';
import { Employee } from '../../../employees/interfaces/employee.interface';

import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { TextareaModule } from 'primeng/textarea';
import { TagModule } from 'primeng/tag';
import { CurrencyPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-loan-application-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    ButtonModule,
    MessageModule,
    TextareaModule,
    TagModule,
    CurrencyPipe,
    DatePipe
  ],
  templateUrl: './loan-application-form.component.html',
  styleUrl: './loan-application-form.component.css'
})
export class LoanApplicationFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly loanService = inject(LoanApplicationService);

  readonly application = input<LoanApplication | null>(null);
  readonly customers = input.required<Customer[]>();
  readonly employees = input.required<Employee[]>();

  readonly saved = output<LoanApplication>();
  readonly cancelled = output<void>();

  visible = signal(true);
  loading = signal(false);
  errorMessage = signal<string | undefined>(undefined);

  form!: FormGroup;
  resolutionForm!: FormGroup;

  fullCustomer: Customer | undefined;
  currentStatus = 'PENDING';

  get app() { return this.application(); }
  get isEditMode(): boolean { return !!this.app; }
  get canBeEvaluated(): boolean { return this.isEditMode && this.currentStatus === 'PENDING'; }

  readonly statusOptions = [
    { label: 'Aprobar', value: 'APPROVED' },
    { label: 'Rechazar', value: 'REJECTED' }
  ];

  ngOnInit(): void {
    const a = this.app;
    this.currentStatus = a?.status || 'PENDING';

    if (a && this.customers()) {
      this.fullCustomer = this.customers().find(x => x.id === a.customerId);
    }

    // Form used ONLY for creating new applications
    this.form = this.fb.group({
      customerId:      [null, [Validators.required]],
      requestedAmount: [null, [Validators.required, Validators.min(0.01)]],
      termInMonths:    [null, [Validators.required, Validators.min(1)]]
    });

    // Form used ONLY for evaluation
    this.resolutionForm = this.fb.group({
      status: [null, [Validators.required]],
      approvedAmount: [null],
      resolutionDetails: [''],
      evaluatedById: [null, [Validators.required]]
    });

    // Handle status changes to require approvedAmount and pre-fill
    this.resolutionForm.get('status')?.valueChanges.subscribe(status => {
      const amountCtrl = this.resolutionForm.get('approvedAmount');
      if (status === 'APPROVED') {
        amountCtrl?.setValidators([Validators.required, Validators.min(0.01)]);
        if (!amountCtrl?.value && this.app?.requestedAmount) {
          amountCtrl?.setValue(this.app.requestedAmount);
        }
      } else {
        amountCtrl?.clearValidators();
        amountCtrl?.setValue(null);
      }
      amountCtrl?.updateValueAndValidity();
    });
  }

  onSubmitBasic(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(undefined);

    const payload = this.form.getRawValue();

    this.loanService.create(payload).subscribe({
      next: (saved) => {
        this.loading.set(false);
        this.saved.emit(saved);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Ocurrió un error al crear la solicitud.');
      }
    });
  }

  onSubmitResolution(): void {
    if (this.resolutionForm.invalid || !this.app) {
      this.resolutionForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(undefined);

    const dto: LoanApplicationResolution = this.resolutionForm.getRawValue();

    this.loanService.resolve(this.app.id, dto).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.saved.emit({ ...this.app!, status: dto.status, resolutionDetails: dto.resolutionDetails, evaluatedBy: dto.evaluatedById });
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Ocurrió un error al evaluar la solicitud.');
      }
    });
  }

  onClose(): void {
    this.cancelled.emit();
  }

  isInvalid(formGroup: FormGroup, name: string): boolean {
    const ctrl = formGroup.get(name);
    return !!(ctrl?.invalid && ctrl.touched);
  }
}
