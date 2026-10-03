import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { InvoiceService } from '../../core/services/invoice.service';
import { NotificationService } from '../../core/services/notification.service';
import { CopCurrencyPipe } from '../../shared/cop-currency.pipe';

type Step = 'form' | 'preview' | 'done';

/** Campos de dinero cuyo texto hay que limpiar antes de calcular. */
type CampoMonto = 'baseValue' | 'shippingValue' | 'shippingAssumedByCompany' | 'warrantyDeduction';

/**
 * Los montos se escriben en campos de texto, no en <input type="number">.
 * Un campo numerico cambia su valor al girar la rueda del raton por
 * encima: basta con desplazar la pagina con el cursor sobre el monto para
 * alterarlo sin darse cuenta, y eso termina en una factura enviada al
 * cliente con una cifra equivocada.
 *
 * Como el formulario guarda cadenas, todo calculo pasa primero por estas
 * dos funciones. En pesos no se usan centavos, por eso los montos se leen
 * como enteros y el porcentaje admite decimales.
 */
function aEntero(valor: string | null | undefined): number {
  const digitos = (valor ?? '').replace(/\D/g, '');
  return digitos === '' ? 0 : Number(digitos);
}

function aDecimal(valor: string | null | undefined): number {
  const limpio = (valor ?? '').replace(',', '.').replace(/[^\d.]/g, '');
  const numero = Number.parseFloat(limpio);
  return Number.isFinite(numero) ? numero : 0;
}

function montoMinimo(minimo: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null =>
    aEntero(control.value) < minimo ? { montoMinimo: { minimo } } : null;
}

function porcentajeEntre(minimo: number, maximo: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = aDecimal(control.value);
    return valor < minimo || valor > maximo ? { porcentaje: { minimo, maximo } } : null;
  };
}

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

  form = this.fb.nonNullable.group({
    clientName: ['', Validators.required],
    baseValue: ['', [Validators.required, montoMinimo(1)]],
    discountPercentage: ['', porcentajeEntre(0, 100)],
    shippingValue: ['', montoMinimo(0)],
    shippingAssumedByCompany: ['', montoMinimo(0)],
    warrantyDeduction: ['', montoMinimo(0)],
  });

  // Senal real: se actualiza en cada cambio del formulario, por eso los
  // computed() de abajo se recalculan solos sin necesidad de ningun boton.
  private formValue = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });

  // Valores ya convertidos a numero. La plantilla usa estos, nunca el
  // texto crudo del formulario.
  baseValue = computed(() => aEntero(this.formValue().baseValue));
  shippingValue = computed(() => aEntero(this.formValue().shippingValue));
  shippingAssumed = computed(() => aEntero(this.formValue().shippingAssumedByCompany));
  warranty = computed(() => aEntero(this.formValue().warrantyDeduction));
  discountPercentage = computed(() => aDecimal(this.formValue().discountPercentage));

  discountValue = computed(() => round2((this.baseValue() * this.discountPercentage()) / 100));

  valueAfterDiscount = computed(() => round2(this.baseValue() - this.discountValue()));

  // El flete asumido por El Puntazo y la garantia/devolucion RESTAN del
  // total: son valores que la empresa cubre o descuenta, no el comprador.
  total = computed(() =>
    round2(this.valueAfterDiscount() + this.shippingValue() - this.shippingAssumed() - this.warranty()),
  );

  shippingAssumedExceedsShipping = computed(() => this.shippingAssumed() > this.shippingValue());

  totalWouldBeNegative = computed(() => this.total() < 0);

  /** Al salir del campo se deja solo lo que es un numero. */
  normalizarMonto(campo: CampoMonto): void {
    const control = this.form.controls[campo];
    const limpio = aEntero(control.value);
    control.setValue(limpio === 0 ? '' : String(limpio));
  }

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

    this.invoiceService
      .create({
        clientName: this.form.controls.clientName.value.trim(),
        baseValue: this.baseValue(),
        discountPercentage: this.discountPercentage(),
        shippingValue: this.shippingValue(),
        shippingAssumedByCompany: this.shippingAssumed(),
        warrantyDeduction: this.warranty(),
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
      baseValue: '',
      discountPercentage: '',
      shippingValue: '',
      shippingAssumedByCompany: '',
      warrantyDeduction: '',
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
