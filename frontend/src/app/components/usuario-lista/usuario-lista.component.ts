import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UsuarioService, Usuario, UsuarioRequest } from '../../services/usuario.service';
import { PerfilService, Perfil } from '../../services/perfil.service';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

@Component({
  selector: 'app-usuario-lista',
  standalone: true,
  imports: [CommonModule, FormsModule, HasPermissionDirective],
  templateUrl: './usuario-lista.component.html',
  styleUrls: ['./usuario-lista.component.css']
})
export class UsuarioListaComponent implements OnInit {
  
  usuarios: Usuario[] = [];
  perfis: Perfil[] = [];
  carregando = false;
  
  // MODAL
  mostrarModal = false;
  modalTitulo = '';
  editando = false;
  usuarioSelecionado?: Usuario;
  
  // FORMULÁRIO
  formulario: UsuarioRequest = {
    nome: '',
    username: '',
    email: '',
    password: '',
    ativo: true,
    perfilIds: []
  };
  
  // FILTROS
  filtroNome = '';
  filtroAtivo = 'todos'; // todos, ativos, inativos
  
  constructor(
    private usuarioService: UsuarioService,
    private perfilService: PerfilService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.carregarUsuarios();
    this.carregarPerfis();
  }
  
  carregarUsuarios(): void {
    this.carregando = true;
    this.usuarioService.listarTodos().subscribe({
      next: (usuarios) => {
        this.usuarios = usuarios;
        this.carregando = false;
      },
      error: (error) => {
        console.error('Erro ao carregar usuários:', error);
        alert('Erro ao carregar usuários!');
        this.carregando = false;
      }
    });
  }
  
  carregarPerfis(): void {
    this.perfilService.listarTodos().subscribe({
      next: (perfis) => {
        this.perfis = perfis;
      },
      error: (error) => {
        console.error('Erro ao carregar perfis:', error);
      }
    });
  }
  
  get usuariosFiltrados(): Usuario[] {
    return this.usuarios.filter(u => {
      const matchNome = !this.filtroNome || 
        u.nome.toLowerCase().includes(this.filtroNome.toLowerCase()) ||
        u.username.toLowerCase().includes(this.filtroNome.toLowerCase()) ||
        u.email.toLowerCase().includes(this.filtroNome.toLowerCase());
      
      const matchAtivo = this.filtroAtivo === 'todos' || 
        (this.filtroAtivo === 'ativos' && u.ativo) ||
        (this.filtroAtivo === 'inativos' && !u.ativo);
      
      return matchNome && matchAtivo;
    });
  }
  
  abrirModalNovo(): void {
    this.editando = false;
    this.modalTitulo = 'Novo Usuário';
    this.formulario = {
      nome: '',
      username: '',
      email: '',
      password: '',
      ativo: true,
      perfilIds: []
    };
    this.mostrarModal = true;
  }
  
  abrirModalEditar(usuario: Usuario): void {
    this.editando = true;
    this.modalTitulo = 'Editar Usuário';
    this.usuarioSelecionado = usuario;
    
    // Carregar dados do usuário para o formulário
    this.usuarioService.buscarPorId(usuario.id!).subscribe({
      next: (u) => {
        this.formulario = {
          nome: u.nome,
          username: u.username,
          email: u.email,
          password: '',
          ativo: u.ativo,
          perfilIds: [] // TODO: Implementar quando backend retornar IDs dos perfis
        };
        this.mostrarModal = true;
      },
      error: (error) => {
        console.error('Erro ao carregar usuário:', error);
        alert('Erro ao carregar dados do usuário!');
      }
    });
  }
  
  fecharModal(): void {
    this.mostrarModal = false;
    this.usuarioSelecionado = undefined;
  }
  
  salvar(): void {
    if (!this.validarFormulario()) {
      return;
    }
    
    if (this.editando && this.usuarioSelecionado) {
      this.atualizar();
    } else {
      this.criar();
    }
  }
  
  validarFormulario(): boolean {
    if (!this.formulario.nome.trim()) {
      alert('Nome é obrigatório!');
      return false;
    }
    
    if (!this.formulario.username.trim()) {
      alert('Username é obrigatório!');
      return false;
    }
    
    if (!this.formulario.email.trim()) {
      alert('E-mail é obrigatório!');
      return false;
    }
    
    if (!this.editando && !this.formulario.password.trim()) {
      alert('Senha é obrigatória para novos usuários!');
      return false;
    }
    
    if (this.formulario.perfilIds.length === 0) {
      alert('Selecione pelo menos um perfil!');
      return false;
    }
    
    return true;
  }
  
  criar(): void {
    this.usuarioService.criar(this.formulario).subscribe({
      next: () => {
        alert('✅ Usuário criado com sucesso!');
        this.fecharModal();
        this.carregarUsuarios();
      },
      error: (error) => {
        console.error('Erro ao criar usuário:', error);
        alert('❌ Erro ao criar usuário: ' + (error.error?.message || error.message));
      }
    });
  }
  
  atualizar(): void {
    this.usuarioService.atualizar(this.usuarioSelecionado!.id!, this.formulario).subscribe({
      next: () => {
        alert('✅ Usuário atualizado com sucesso!');
        this.fecharModal();
        this.carregarUsuarios();
      },
      error: (error) => {
        console.error('Erro ao atualizar usuário:', error);
        alert('❌ Erro ao atualizar usuário: ' + (error.error?.message || error.message));
      }
    });
  }
  
  excluir(usuario: Usuario): void {
    if (!confirm(`Deseja realmente excluir o usuário "${usuario.nome}"?`)) {
      return;
    }
    
    this.usuarioService.deletar(usuario.id!).subscribe({
      next: () => {
        alert('✅ Usuário excluído com sucesso!');
        this.carregarUsuarios();
      },
      error: (error) => {
        console.error('Erro ao excluir usuário:', error);
        alert('❌ Erro ao excluir usuário: ' + (error.error?.message || error.message));
      }
    });
  }
  
  toggleAtivo(usuario: Usuario): void {
    const acao = usuario.ativo ? 'desativar' : 'ativar';
    
    if (!confirm(`Deseja realmente ${acao} o usuário "${usuario.nome}"?`)) {
      return;
    }
    
    this.usuarioService.ativarDesativar(usuario.id!).subscribe({
      next: () => {
        alert(`✅ Usuário ${acao}do com sucesso!`);
        this.carregarUsuarios();
      },
      error: (error) => {
        console.error(`Erro ao ${acao} usuário:`, error);
        alert(`❌ Erro ao ${acao} usuário!`);
      }
    });
  }
  
  togglePerfil(perfilId: number): void {
    const index = this.formulario.perfilIds.indexOf(perfilId);
    if (index > -1) {
      this.formulario.perfilIds.splice(index, 1);
    } else {
      this.formulario.perfilIds.push(perfilId);
    }
  }
  
  perfilSelecionado(perfilId: number): boolean {
    return this.formulario.perfilIds.includes(perfilId);
  }

  voltar(): void {
  this.router.navigate(['/dashboard']);
}
}