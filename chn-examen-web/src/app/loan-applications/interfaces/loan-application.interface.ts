export interface LoanApplication {
  id: number;
  customerId: number;
  requestedAmount: number;
  termInMonths: number;
  status?: string;
  resolutionDetails?: string;
  applicationDate?: Date;
  evaluatedBy?: number;
}

export interface LoanApplicationResolution {
  status: string;
  resolutionDetails: string;
  evaluatedById?: number; // Depending on how the backend uses it
  approvedAmount?: number;
}
