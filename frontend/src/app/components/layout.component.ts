import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar.component';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  template: `
    <div class="layout">
      <app-sidebar *ngIf="!isRestaurante"></app-sidebar>
      <main class="main-content" [class.sem-sidebar]="isRestaurante">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .layout {
      display: flex;
      min-height: 100vh;
      background: #ecf0f1;
    }
    .main-content {
      flex: 1;
      margin-left: 200px;
      min-height: 100vh;
      transition: margin-left 0.3s ease;
      position: relative;
    }
    .main-content.sem-sidebar {
      margin-left: 0;
    }
    @media (max-width: 768px) {
      .main-content {
        margin-left: 70px;
      }
    }
    @media print {
      app-sidebar {
        display: none !important;
      }
      .main-content {
        margin-left: 0 !important;
        width: 100% !important;
      }
      .layout {
        background: white !important;
      }
      app-indicador-caixa {
        display: none !important;
      }
    }
  `]
})
export class LayoutComponent {
  private authService = inject(AuthService);

 get isRestaurante(): boolean {
  return this.authService.hasPerfil('RESTAURANTE') || this.authService.hasPerfil('COZINHA');
}
}