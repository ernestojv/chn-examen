import { Component, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UserService } from '../services/user.service';
import { User } from '../interfaces/user.interface';
import { UserFormComponent } from '../components/user-form/user-form.component';

import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { ToastModule } from 'primeng/toast';
import { SkeletonModule } from 'primeng/skeleton';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-users-page',
  standalone: true,
  providers: [MessageService, ConfirmationService],
  imports: [
    FormsModule,
    TableModule,
    InputTextModule,
    ButtonModule,
    IconFieldModule,
    InputIconModule,
    ToastModule,
    SkeletonModule,
    ConfirmDialogModule,
    UserFormComponent
  ],
  templateUrl: './users-page.component.html',
  styleUrl: './users-page.component.css'
})
export class UsersPageComponent {
  private readonly userService = inject(UserService);
  private readonly messageService = inject(MessageService);
  private readonly confirmationService = inject(ConfirmationService);

  readonly users = signal<User[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');

  readonly formUser = signal<User | null | undefined>(undefined);
  readonly showForm = signal(false);

  readonly filteredUsers = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.users();
    return this.users().filter(u =>
      u.username.toLowerCase().includes(query)
    );
  });

  constructor() {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.loading.set(true);
    this.userService.getAll().subscribe({
      next: (data) => {
        this.users.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onNew(): void {
    this.formUser.set(null);
    this.showForm.set(true);
  }

  onEdit(user: User): void {
    this.formUser.set(user);
    this.showForm.set(true);
  }

  onDelete(user: User): void {
    this.confirmationService.confirm({
      message: `¿Estás seguro de que deseas eliminar al usuario <strong>${user.username}</strong>? Esta acción no se puede deshacer.`,
      header: 'Confirmar eliminación',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Cancelar',
      acceptButtonProps: { severity: 'danger' },
      accept: () => {
        this.userService.delete(user.id).subscribe({
          next: () => {
            this.users.update(list => list.filter(u => u.id !== user.id));
            this.messageService.add({
              severity: 'success',
              summary: 'Usuario eliminado',
              detail: user.username,
              life: 3000
            });
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: 'No se pudo eliminar el usuario.',
              life: 4000
            });
          }
        });
      }
    });
  }

  onSaved(saved: User): void {
    const isEdit = !!this.formUser();
    if (isEdit) {
      this.users.update(list => list.map(u => u.id === saved.id ? saved : u));
    } else {
      this.users.update(list => [...list, saved]);
    }
    this.showForm.set(false);
    this.messageService.add({
      severity: 'success',
      summary: isEdit ? 'Usuario actualizado' : 'Usuario creado',
      detail: saved.username,
      life: 3000
    });
  }

  onCancelled(): void {
    this.showForm.set(false);
  }
}
