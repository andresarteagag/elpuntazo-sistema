import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClientService } from '../../core/services/client.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { Client } from '../../core/models/client.model';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css',
})
export class ClientesComponent {
  private fb = inject(FormBuilder);
  private clientService = inject(ClientService);
  private notifications = inject(NotificationService);
  auth = inject(AuthService);

  clients = signal<Client[]>([]);
  loading = signal(true);
  searchTerm = signal('');

  showForm = signal(false);
  editingClient = signal<Client | null>(null);

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    identification: [''],
    phone: [''],
    email: [''],
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.clientService.search(this.searchTerm()).subscribe({
      next: (page) => {
        this.clients.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onSearchInput(term: string): void {
    this.searchTerm.set(term);
    this.load();
  }

  openCreate(): void {
    this.editingClient.set(null);
    this.form.reset({ name: '', identification: '', phone: '', email: '' });
    this.showForm.set(true);
  }

  openEdit(client: Client): void {
    this.editingClient.set(client);
    this.form.reset({
      name: client.name,
      identification: client.identification ?? '',
      phone: client.phone ?? '',
      email: client.email ?? '',
    });
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const editing = this.editingClient();

    const request$ = editing
      ? this.clientService.update(editing.id, value)
      : this.clientService.create(value);

    request$.subscribe({
      next: () => {
        this.notifications.show(
          editing ? 'Cliente actualizado correctamente.' : 'Cliente creado correctamente.',
          'success',
        );
        this.showForm.set(false);
        this.load();
      },
    });
  }

  toggleActive(client: Client): void {
    this.clientService.setActive(client.id, !client.active).subscribe({
      next: () => {
        this.notifications.show(
          client.active ? 'Cliente desactivado.' : 'Cliente activado.',
          'success',
        );
        this.load();
      },
    });
  }
}
