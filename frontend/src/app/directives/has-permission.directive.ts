import { Directive, Input, TemplateRef, ViewContainerRef, OnInit } from '@angular/core';
import { PermissionService } from '../services/permission.service';

@Directive({
  selector: '[hasPermission]',
  standalone: true
})
export class HasPermissionDirective implements OnInit {

  private permission: string = '';

  @Input() set hasPermission(permission: string) {
    this.permission = permission;
    this.updateView();
  }

  constructor(
    private templateRef: TemplateRef<any>,
    private viewContainer: ViewContainerRef,
    private permissionService: PermissionService
  ) {}

  ngOnInit(): void {
    this.updateView();
  }

  private updateView(): void {
    this.viewContainer.clear();

    if (this.permissionService.hasPermission(this.permission)) {
      // ✅ TEM permissão → mostra o elemento
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      // ❌ NÃO TEM permissão → oculta o elemento
      // viewContainer fica vazio
    }
  }
}