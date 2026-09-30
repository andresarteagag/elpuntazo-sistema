import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'cop', standalone: true })
export class CopCurrencyPipe implements PipeTransform {
  transform(value: number | null | undefined): string {
    if (value === null || value === undefined || isNaN(value)) {
      return '$0';
    }
    const rounded = Math.round(value);
    const formatted = rounded.toLocaleString('es-CO', { maximumFractionDigits: 0 });
    return `$${formatted}`;
  }
}
