import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { InvoiceService } from '../../core/services/invoice.service';
import { NotificationService } from '../../core/services/notification.service';
import { CopCurrencyPipe } from '../../shared/cop-currency.pipe';

type Step = 'form' | 'preview' | 'done';

@Component({
  selector: 'app-nueva-factura',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CopCurrencyPipe],
  templateUrl: './nueva-factura.component.html',
  styleUrl: './nueva-factura.component.css',
})
export class NuevaFacturaComponent {
  private fb = inject(FormBuilder);
  private invoiceService = inject(InvoiceService);
  private notifications = inject(NotificationService);
  private router = inject(Router);

  step = signal<Step>('form');
  saving = signal(false);
  createdInvoiceId = signal<number | null>(null);
  createdInvoiceNumber = signal<string | null>(null);

  // --- Formulario de la factura: el nombre del cliente es simplemente
  // texto libre -- el vendedor no busca ni crea un registro de cliente ---
  form = this.fb.nonNullable.group({
    clientName: ['', Validators.required],
    baseValue: [null as number | null, [Validators.required, Validators.min(1)]],
    discountPercentage: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    shippingValue: [0, [Validators.required, Validators.min(0)]],
    shippingAssumedByCompany: [0, [Validators.required, Validators.min(0)]],
    warrantyDeduction: [0, [Validators.required, Validators.min(0)]],
  });

  // --- Calculo en tiempo real: formValue es una senal real que se
  // actualiza en cada cambio del formulario, por eso los computed()
  // de abajo se recalculan solos sin necesidad de ningun boton ---
  private formValue = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  discountValue = computed(() => {
    const v = this.formValue();
    const base = v.baseValue ?? 0;
    const pct = v.discountPercentage ?? 0;
    return round2((base * pct) / 100);
  });

  valueAfterDiscount = computed(() => {
    const v = this.formValue();
    const base = v.baseValue ?? 0;
    return round2(base - this.discountValue());
  });

  // El flete asumido por El Puntazo y la garantia/devolucion RESTAN del
  // total -- son valores que la empresa cubre o descuenta, no el comprador.
  total = computed(() => {
    const v = this.formValue();
    const shipping = v.shippingValue ?? 0;
    const assumed = v.shippingAssumedByCompany ?? 0;
    const warranty = v.warrantyDeduction ?? 0;
    return round2(this.valueAfterDiscount() + shipping - assumed - warranty);
  });

  shippingAssumedExceedsShipping = computed(() => {
    const v = this.formValue();
    return (v.shippingAssumedByCompany ?? 0) > (v.shippingValue ?? 0);
  });

  totalWouldBeNegative = computed(() => this.total() < 0);

  constructor() {}

  goToPreview(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notifications.show('Revisa los datos ingresados.', 'error');
      return;
    }
    if (this.shippingAssumedExceedsShipping()) {
      this.notifications.show('El valor asumido por El Puntazo no puede ser mayor al flete total.', 'error');
      return;
    }
    if (this.totalWouldBeNegative()) {
      this.notifications.show('El total no puede quedar negativo. Revisa los valores ingresados.', 'error');
      return;
    }
    this.step.set('preview');
  }

  backToForm(): void {
    this.step.set('form');
  }

  finalize(): void {
    this.saving.set(true);
    const { clientName, baseValue, discountPercentage, shippingValue, shippingAssumedByCompany, warrantyDeduction } =
      this.form.getRawValue();

    this.invoiceService
      .create({
        clientName: clientName.trim(),
        baseValue: baseValue ?? 0,
        discountPercentage: discountPercentage ?? 0,
        shippingValue: shippingValue ?? 0,
        shippingAssumedByCompany: shippingAssumedByCompany ?? 0,
        warrantyDeduction: warrantyDeduction ?? 0,
      })
      .subscribe({
        next: (invoice) => {
          this.createdInvoiceId.set(invoice.id);
          this.createdInvoiceNumber.set(invoice.invoiceNumber);
          this.step.set('done');
          this.saving.set(false);
        },
        error: () => this.saving.set(false),
      });
  }

  downloadPdf(): void {
    const id = this.createdInvoiceId();
    const number = this.createdInvoiceNumber();
    if (id && number) {
      this.invoiceService.downloadPdf(id, number);
    }
  }

  startAnother(): void {
    this.form.reset({
      clientName: '',
      baseValue: null,
      discountPercentage: 0,
      shippingValue: 0,
      shippingAssumedByCompany: 0,
      warrantyDeduction: 0,
    });
    this.createdInvoiceId.set(null);
    this.createdInvoiceNumber.set(null);
    this.step.set('form');
  }

  goToHistorial(): void {
    this.router.navigate(['/facturas']);
  }
}

function round2(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}
