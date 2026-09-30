import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { SellerService } from '../../../core/services/seller.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Seller } from '../../../core/models/seller.model';

@Component({
  selector: 'app-vendedores',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './vendedores.component.html',
  styleUrl: './vendedores.component.css',
})
export class VendedoresComponent {
  private fb = inject(FormBuilder);
  private sellerService = inject(SellerService);
  private notifications = inject(NotificationService);

  sellers = signal<Seller[]>([]);
  loading = signal(true);

  showForm = signal(false);
  editingSeller = signal<Seller | null>(null);

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.sellerService.listAll().subscribe({
      next: (sellers) => {
        this.sellers.set(sellers);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openCreate(): void {
    this.editingSeller.set(null);
    this.form.reset({ name: '', email: '', password: '' });
    this.form.controls.password.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
    this.form.controls.email.enable();
    this.showForm.set(true);
  }

  openEdit(seller: Seller): void {
    this.editingSeller.set(seller);
    this.form.reset({ name: seller.name, email: seller.email, password: '' });
    this.form.controls.password.setValidators([Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
    this.form.controls.email.disable();
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

    const editing = this.editingSeller();
    const value = this.form.getRawValue();

    const request$ = editing
      ? this.sellerService.update(editing.id, { name: value.name, newPassword: value.password || undefined })
      : this.sellerService.create({ name: value.name, email: value.email, password: value.password });

    request$.subscribe({
      next: () => {
        this.notifications.show(
          editing ? 'Vendedor actualizado correctamente.' : 'Vendedor creado correctamente.',
          'success',
        );
        this.showForm.set(false);
        this.load();
      },
    });
  }

  toggleActive(seller: Seller): void {
    this.sellerService.setActive(seller.id, !seller.active).subscribe({
      next: () => {
        this.notifications.show(
          seller.active ? 'Vendedor desactivado. Ya no podra iniciar sesion.' : 'Vendedor activado.',
          'success',
        );
        this.load();
      },
    });
  }
}
