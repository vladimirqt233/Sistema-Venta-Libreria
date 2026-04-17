import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {
  usuarios: any[] = [];
  clientes: any[] = [];
  vendedores: any[] = [];
  selectedUserId: number | null = null;

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsuarios();
    this.loadClientes();
    this.loadVendedores();
  }

  loadUsuarios(): void {
    this.http.get('http://localhost:8095/usuario/listar').subscribe((data: any) => {
      this.usuarios = data;
    });
  }

  loadClientes(): void {
    this.http.get('http://localhost:8095/cliente').subscribe((data: any) => {
      this.clientes = data;
    });
  }

  loadVendedores(): void {
    this.http.get('http://localhost:8095/vendedor').subscribe((data: any) => {
      this.vendedores = data;
    });
  }

  deleteUser(id: number): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.http.delete(`http://localhost:8095/usuario/eliminar/${id}`).subscribe(() => {
        this.loadUsuarios();
      });
    }
  }

  assignClientOrVendor(userId: number, type: 'cliente' | 'vendedor', event: Event): void {
    const entityId = (event.target as HTMLSelectElement).value;
    const url =
      type === 'cliente'
        ? `http://localhost:8095/usuario/vincularCliente/${userId}/${entityId}`
        : `http://localhost:8095/usuario/vincularVendedor/${userId}/${entityId}`;

    this.http.post(url, {}).subscribe(() => {
      this.loadUsuarios();
    });
  }

  openAssignModal(userId: number): void {
    this.selectedUserId = userId;
  }
}
