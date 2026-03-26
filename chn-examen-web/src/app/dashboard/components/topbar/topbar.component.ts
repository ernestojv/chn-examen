import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../auth/services/auth.service';

import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-topbar',
  imports: [AvatarModule, MenuModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.css'
})
export class TopbarComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  /** Username decoded from the stored JWT token. */
  readonly username = signal<string>(this.resolveUsername());

  readonly userMenuItems: MenuItem[] = [
    {
      label: 'Cerrar sesión',
      icon: 'pi pi-sign-out',
      command: () => this.logout()
    }
  ];

  private resolveUsername(): string {
    const token = this.authService.getToken();
    if (!token) return 'Usuario';
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      return decoded.sub ?? 'Usuario';
    } catch {
      return 'Usuario';
    }
  }

  private logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
