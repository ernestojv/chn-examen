export interface Loan {
  id: number;
  loanApplicationId: number;
  customerId: number;
  approvedAmount: number;
  outstandingBalance: number;
  paymentStatus: string;
}
