export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  nit: string;
  dateOfBirth: string;
  address?: string;
  email?: string;
  phoneNumber?: string;
}
