import { Component, inject } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { HeaderComponent } from './components/header.component';
import { DepositoProvisorioComponent } from './components/deposito-provisorio/deposito-provisorio.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, DepositoProvisorioComponent, CommonModule],
  template: `
    <!-- Header com nome do usuário -->
    <app-header></app-header>

    <!-- Conteúdo das páginas -->
    <router-outlet></router-outlet>
    <app-deposito-provisorio *ngIf="!isRotaPublica" class="no-print"></app-deposito-provisorio>
  `,
  styles: []
})
export class App {
  title = 'divan';
  private router = inject(Router);

  get isRotaPublica(): boolean {
    return this.router.url.startsWith('/ponto') || this.router.url.startsWith('/login');
  }
}