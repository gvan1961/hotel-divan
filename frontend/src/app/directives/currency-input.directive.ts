import { Directive, ElementRef, HostListener, inject, OnInit } from '@angular/core';
import { NgControl } from '@angular/forms';
 
@Directive({
  selector: '[appCurrencyInput]',
  standalone: true
})
export class CurrencyInputDirective implements OnInit {
  private el = inject(ElementRef);
  private ngControl = inject(NgControl, { optional: true });
 
  // ✅ NOVO: formata o valor inicial, caso o campo já venha
  // pré-preenchido programaticamente (ex: valor padrão = saldo da
  // conta) — antes, só formatava quando o usuário digitava algo.
  ngOnInit(): void {
    setTimeout(() => {
      const valorInicial = this.ngControl?.control?.value;
      if (valorInicial !== null && valorInicial !== undefined && !isNaN(Number(valorInicial))) {
        this.el.nativeElement.value = this.formatarMoeda(Number(valorInicial));
      }
    });
  }
 
  @HostListener('input', ['$event'])
  onInput(event: any): void {
    // Pegar apenas números
    let numeros = event.target.value.replace(/\D/g, '');
    // Se vazio, zerar
    if (!numeros || numeros === '') {
      numeros = '0';
    }
    // Converter para centavos
    const valorEmCentavos = parseInt(numeros, 10);
    const valorEmReais = valorEmCentavos / 100;
    // Atualizar o modelo com valor numérico
    if (this.ngControl?.control) {
      this.ngControl.control.setValue(valorEmReais, { emitEvent: false });
    }
    // Formatar display
    event.target.value = this.formatarMoeda(valorEmReais);
  }
 
  @HostListener('focus')
  onFocus(): void {
    // Selecionar tudo ao focar (facilita digitação)
    setTimeout(() => this.el.nativeElement.select(), 0);
  }
 
  @HostListener('blur')
  onBlur(): void {
    // Garantir formatação ao sair
    const valorAtual = this.el.nativeElement.value.replace(/\D/g, '');
    if (!valorAtual || valorAtual === '0' || valorAtual === '') {
      this.el.nativeElement.value = 'R$ 0,00';
      if (this.ngControl?.control) {
        this.ngControl.control.setValue(0, { emitEvent: false });
      }
    }
  }
 
  private formatarMoeda(valor: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(valor);
  }
}