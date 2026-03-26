import { Component, inject, input, output, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaymentService } from '../../services/payment.service';
import { Loan } from '../../../loans/interfaces/loan.interface';
import { Customer } from '../../../customers/interfaces/customer.interface';
import { AuthService } from '../../../auth/services/auth.service';
import { UserService } from '../../../users/services/user.service';

import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

// Component to handle payment registration
@Component({
  selector: 'app-payment-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DialogModule,
    InputNumberModule,
    SelectModule,
    ButtonModule,
    MessageModule
  ],
  templateUrl: './payment-form.component.html',
  styleUrl: './payment-form.component.css'
})
export class PaymentFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly paymentService = inject(PaymentService);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);

  readonly loans = input.required<Loan[]>();
  readonly customers = input.required<Customer[]>();

  readonly saved = output<void>();
  readonly cancelled = output<void>();

  visible = signal(true);
  loading = signal(false);
  errorMessage = signal<string | undefined>(undefined);

  form!: FormGroup;
  currentUserId: number | null = null;

  readonly methodOptions = [
    { label: 'Efectivo', value: 'Efectivo' },
    { label: 'Transferencia', value: 'Transferencia' },
    { label: 'Cheque', value: 'Cheque' },
    { label: 'Otros', value: 'Otros' }
  ];

  ngOnInit(): void {
    this.form = this.fb.group({
      loanId:        [null, [Validators.required]],
      amountPaid:    [null, [Validators.required, Validators.min(0.01)]],
      paymentMethod: [null, [Validators.required]]
    });

    this.resolveCurrentUserId();
  }

  private resolveCurrentUserId(): void {
    const username = this.authService.getUsername();
    if (username) {
      this.userService.getAll().subscribe(users => {
        const found = users.find(u => u.username === username);
        if (found) this.currentUserId = found.id;
      });
    }
  }

  getLoanLabel(loan: Loan): string {
    const cust = this.customers().find(c => c.id === loan.customerId);
    return `ID: ${loan.id} - ${cust?.firstName || ''} ${cust?.lastName || ''} (Saldo: ${loan.outstandingBalance})`;
  }

  onSubmit(): void {
    if (this.form.invalid || !this.currentUserId) {
      if (!this.currentUserId) this.errorMessage.set('Sesión no válida para este registro.');
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(undefined);

    const payload = {
      ...this.form.getRawValue(),
      registeredById: this.currentUserId
    };

    this.paymentService.create(payload).subscribe({
      next: () => {
        this.loading.set(false);
        this.saved.emit();
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Error al registrar el pago.');
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
