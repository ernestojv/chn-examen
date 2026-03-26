import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  private readonly router = inject(Router);

  readonly navItems: NavItem[] = [
    { label: 'Clientes',          icon: 'pi pi-users',        route: '/dashboard/customers' },
    { label: 'Empleados',         icon: 'pi pi-id-card',      route: '/dashboard/employees' },
    { label: 'Usuarios',          icon: 'pi pi-user',         route: '/dashboard/users' },
    { label: 'Solicitudes',       icon: 'pi pi-file',         route: '/dashboard/loan-applications' },
    { label: 'Préstamos',         icon: 'pi pi-credit-card',  route: '/dashboard/loans' },
    { label: 'Pagos',             icon: 'pi pi-wallet',       route: '/dashboard/payments' },
  ];
}
