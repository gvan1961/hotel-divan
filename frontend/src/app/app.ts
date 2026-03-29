import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header.component';
import { DepositoProvisorioComponent } from './components/deposito-provisorio/deposito-provisorio.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, DepositoProvisorioComponent],
  template: `
    <!-- Header com nome do usuário -->
    <app-header></app-header>
    
    <!-- Conteúdo das páginas -->
    <router-outlet></router-outlet>
    <app-deposito-provisorio></app-deposito-provisorio>
  `,
  styles: []
})
export class App {
  title = 'divan';
}
