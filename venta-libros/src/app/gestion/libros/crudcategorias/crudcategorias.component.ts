import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-crudcategorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crudcategorias.component.html',
  styleUrl: './crudcategorias.component.css'
})
export class CrudcategoriasComponent implements OnInit {
  categorias: any[] = [];
  isModalOpen: boolean = false;
  modalTitle: string = '';
  selectedCategoria: any = {
    id: null,
    nombre: '',
    clasificacion: '',
    formato: '',
    idioma: ''
  };
  formatos: string[] = ['IMPRESO', 'ELECTRONICO', 'AUDIOLIBRO'];

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadCategorias();
  }

  loadCategorias(): void {
    this.http.get('http://localhost:8095/categoria').subscribe((data: any) => {
      this.categorias = data;
    });
  }

  openModal(mode: string, categoria?: any): void {
    this.isModalOpen = true;
    this.modalTitle = mode === 'create' ? 'Agregar Categoría' : 'Editar Categoría';

    if (mode === 'edit' && categoria) {
      this.selectedCategoria = { ...categoria };
    } else {
      this.selectedCategoria = {
        id: null,
        nombre: '',
        clasificacion: '',
        formato: '',
        idioma: ''
      };
    }
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  saveCategoria(): void {
    if (this.selectedCategoria.id) {
      this.http.put(`http://localhost:8095/categoria/${this.selectedCategoria.id}`, this.selectedCategoria).subscribe(() => {
        this.loadCategorias();
        this.closeModal();
      });
    } else {
      this.http.post('http://localhost:8095/categoria', this.selectedCategoria).subscribe(() => {
        this.loadCategorias();
        this.closeModal();
      });
    }
  }

  deleteCategoria(id: number): void {
    this.http.delete(`http://localhost:8095/categoria/${id}`).subscribe(() => {
      this.loadCategorias();
    });
  }
}
