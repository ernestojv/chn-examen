export interface Payment {
  id: number;
  loanId: number;
  amountPaid: number;
  paymentDate?: Date;
  paymentMethod: string;
  registeredById: number;
}
