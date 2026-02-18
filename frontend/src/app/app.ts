import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from './components/header.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent],
  template: `
    <!-- Header com nome do usuário -->
    <app-header></app-header>
    
    <!-- Conteúdo das páginas -->
    <router-outlet></router-outlet>
  `,
  styles: []
})
export class App {
  title = 'divan';
}