import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PerfilService, Perfil, PerfilRequest } from '../../services/perfil.service';
import { PermissaoService, Permissao } from '../../services/permissao.service';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

interface PermissaoPorCategoria {
  categoria: string;
  permissoes: Permissao[];
}

@Component({
  selector: 'app-perfil-lista',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective],
  templateUrl: './perfil-lista.component.html',
  styleUrls: ['./perfil-lista.component.css']
})
export class PerfilListaComponent implements OnInit {
  
  perfis: Perfil[] = [];
  permissoes: Permissao[] = [];
  permissoesPorCategoria: PermissaoPorCategoria[] = [];
  carregando = false;
  
  // MODAL
  mostrarModal = false;
  modalTitulo = '';
  editando = false;
  perfilSelecionado?: Perfil;
  
  // FORMULÁRIO
  formulario: PerfilRequest = {
    nome: '',
    descricao: '',
    permissaoIds: []
  };
  
  // BUSCA E CATEGORIAS EXPANDIDAS
  buscaPermissao = '';
  categoriasExpandidas: Set<string> = new Set();
  
  constructor(
    private perfilService: PerfilService,
    private permissaoService: PermissaoService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.carregarPerfis();
    this.carregarPermissoes();
  }
  
  carregarPerfis(): void {
    this.carregando = true;
    this.perfilService.listarTodos().subscribe({
      next: (perfis) => {
        this.perfis = perfis;
        this.carregando = false;
      },
      error: (error) => {
        console.error('Erro ao carregar perfis:', error);
        alert('Erro ao carregar perfis!');
        this.carregando = false;
      }
    });
  }
  
  carregarPermissoes(): void {
    this.permissaoService.listarTodas().subscribe({
      next: (permissoes) => {
        this.permissoes = permissoes;
        this.agruparPermissoesPorCategoria();
      },
      error: (error) => {
        console.error('Erro ao carregar permissões:', error);
      }
    });
  }
  
  agruparPermissoesPorCategoria(): void {
    const categorias = new Map<string, Permissao[]>();
    
    this.permissoes.forEach(p => {
      if (!categorias.has(p.categoria)) {
        categorias.set(p.categoria, []);
      }
      categorias.get(p.categoria)!.push(p);
    });
    
    this.permissoesPorCategoria = Array.from(categorias.entries())
      .map(([categoria, permissoes]) => ({ categoria, permissoes }))
      .sort((a, b) => a.categoria.localeCompare(b.categoria));
  }
  
  get permissoesFiltradas(): PermissaoPorCategoria[] {
    if (!this.buscaPermissao.trim()) {
      return this.permissoesPorCategoria;
    }
    
    const busca = this.buscaPermissao.toLowerCase();
    
    return this.permissoesPorCategoria
      .map(grupo => ({
        categoria: grupo.categoria,
        permissoes: grupo.permissoes.filter(p => 
          p.nome.toLowerCase().includes(busca) ||
          p.descricao.toLowerCase().includes(busca)
        )
      }))
      .filter(grupo => grupo.permissoes.length > 0);
  }
  
  toggleCategoria(categoria: string): void {
    if (this.categoriasExpandidas.has(categoria)) {
      this.categoriasExpandidas.delete(categoria);
    } else {
      this.categoriasExpandidas.add(categoria);
    }
  }
  
  categoriaExpandida(categoria: string): boolean {
    return this.categoriasExpandidas.has(categoria);
  }
  
  abrirModalNovo(): void {
    this.editando = false;
    this.modalTitulo = 'Novo Perfil';
    this.formulario = {
      nome: '',
      descricao: '',
      permissaoIds: []
    };
    this.categoriasExpandidas.clear();
    this.buscaPermissao = '';
    this.mostrarModal = true;
  }
  
  abrirModalEditar(perfil: Perfil): void {
    this.editando = true;
    this.modalTitulo = 'Editar Perfil';
    this.perfilSelecionado = perfil;
    
    this.perfilService.buscarPorId(perfil.id!).subscribe({
      next: (p) => {
        this.formulario = {
          nome: p.nome,
          descricao: p.descricao,
          permissaoIds: p.permissoes?.map(perm => perm.id) || []
        };
        this.categoriasExpandidas.clear();
        this.buscaPermissao = '';
        this.mostrarModal = true;
      },
      error: (error) => {
        console.error('Erro ao carregar perfil:', error);
        alert('Erro ao carregar perfil!');
      }
    });
  }
  
  fecharModal(): void {
    this.mostrarModal = false;
    this.perfilSelecionado = undefined;
    this.buscaPermissao = '';
  }
  
  salvar(): void {
    if (!this.validarFormulario()) {
      return;
    }
    
    if (this.editando && this.perfilSelecionado) {
      this.atualizar();
    } else {
      this.criar();
    }
  }
  
  validarFormulario(): boolean {
    if (!this.formulario.nome.trim()) {
      alert('Nome do perfil é obrigatório!');
      return false;
    }
    
    if (!this.formulario.descricao.trim()) {
      alert('Descrição é obrigatória!');
      return false;
    }
    
    if (this.formulario.permissaoIds.length === 0) {
      alert('Selecione pelo menos uma permissão!');
      return false;
    }
    
    return true;
  }
  
  criar(): void {
    this.perfilService.criar(this.formulario).subscribe({
      next: () => {
        alert('✅ Perfil criado com sucesso!');
        this.fecharModal();
        this.carregarPerfis();
      },
      error: (error) => {
        console.error('Erro ao criar perfil:', error);
        alert('❌ Erro ao criar perfil: ' + (error.error?.message || error.message));
      }
    });
  }
  
  atualizar(): void {
    this.perfilService.atualizar(this.perfilSelecionado!.id!, this.formulario).subscribe({
      next: () => {
        alert('✅ Perfil atualizado com sucesso!');
        this.fecharModal();
        this.carregarPerfis();
      },
      error: (error) => {
        console.error('Erro ao atualizar perfil:', error);
        alert('❌ Erro ao atualizar perfil: ' + (error.error?.message || error.message));
      }
    });
  }
  
  excluir(perfil: Perfil): void {
    if (['ADMIN', 'RECEPCIONISTA', 'FINANCEIRO'].includes(perfil.nome)) {
      alert('❌ Não é possível excluir perfis do sistema!');
      return;
    }
    
    if (!confirm(`Deseja realmente excluir o perfil "${perfil.nome}"?`)) {
      return;
    }
    
    this.perfilService.deletar(perfil.id!).subscribe({
      next: () => {
        alert('✅ Perfil excluído com sucesso!');
        this.carregarPerfis();
      },
      error: (error) => {
        console.error('Erro ao excluir perfil:', error);
        alert('❌ Erro ao excluir perfil: ' + (error.error?.message || error.message));
      }
    });
  }
  
  togglePermissao(permissaoId: number): void {
    const index = this.formulario.permissaoIds.indexOf(permissaoId);
    if (index > -1) {
      this.formulario.permissaoIds.splice(index, 1);
    } else {
      this.formulario.permissaoIds.push(permissaoId);
    }
  }
  
  permissaoSelecionada(permissaoId: number): boolean {
    return this.formulario.permissaoIds.includes(permissaoId);
  }
  
  selecionarTodasCategoria(categoria: string): void {
    const grupo = this.permissoesPorCategoria.find(g => g.categoria === categoria);
    if (!grupo) return;
    
    const todasSelecionadas = grupo.permissoes.every(p => 
      this.formulario.permissaoIds.includes(p.id)
    );
    
    if (todasSelecionadas) {
      // Desmarcar todas
      grupo.permissoes.forEach(p => {
        const index = this.formulario.permissaoIds.indexOf(p.id);
        if (index > -1) {
          this.formulario.permissaoIds.splice(index, 1);
        }
      });
    } else {
      // Marcar todas
      grupo.permissoes.forEach(p => {
        if (!this.formulario.permissaoIds.includes(p.id)) {
          this.formulario.permissaoIds.push(p.id);
        }
      });
    }
  }
  
  todasSelecionadasCategoria(categoria: string): boolean {
    const grupo = this.permissoesPorCategoria.find(g => g.categoria === categoria);
    if (!grupo) return false;
    
    return grupo.permissoes.every(p => this.formulario.permissaoIds.includes(p.id));
  }
  
  voltar(): void {
    this.router.navigate(['/dashboard']);
  }
}