import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-vendedores',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './vendedores.component.html',
  styleUrl: './vendedores.component.css'
})
export class VendedoresComponent implements OnInit {
  vendedores: any[] = [];
  isModalOpen: boolean = false;
  modalTitle: string = '';
  selectedVendedor: any = {
    id: null,
    nombre: '',
    dni: '',
    telefono: '',
    correo: '',
    direccion: ''
  };

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadVendedores();
  }

  loadVendedores(): void {
    this.http.get('http://localhost:8095/vendedor').subscribe((data: any) => {
      this.vendedores = data;
    });
  }

  openModal(mode: string, vendedor?: any): void {
    this.isModalOpen = true;
    this.modalTitle = mode === 'create' ? 'Agregar Vendedor' : 'Editar Vendedor';

    if (mode === 'edit' && vendedor) {
      this.selectedVendedor = { ...vendedor };
    } else {
      this.selectedVendedor = {
        id: null,
        nombre: '',
        dni: '',
        telefono: '',
        correo: '',
        direccion: ''
      };
    }
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  saveVendedor(): void {
    if (this.selectedVendedor.id) {
      this.http.put(`http://localhost:8095/vendedor/${this.selectedVendedor.id}`, this.selectedVendedor).subscribe(() => {
        this.loadVendedores();
        this.closeModal();
      });
    } else {
      this.http.post('http://localhost:8095/vendedor', this.selectedVendedor).subscribe(() => {
        this.loadVendedores();
        this.closeModal();
      });
    }
  }

  deleteVendedor(id: number): void {
    this.http.delete(`http://localhost:8095/vendedor/${id}`).subscribe(() => {
      this.loadVendedores();
    });
  }
}
