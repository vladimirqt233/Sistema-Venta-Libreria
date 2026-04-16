import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-crudcategorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crudcategorias.component.html',
  styleUrl: './crudcategorias.component.css'
})
export class CrudcategoriasComponent {
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
  formatos = ['IMPRESO', 'ELECTRONICO', 'AUDIOLIBRO'];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadCategorias();
  }f

  loadCategorias() {
    this.http.get('http://localhost:8095/categoria').subscribe((data: any) => {
      this.categorias = data;
    });
  }

  openModal(mode: string, categoria?: any) {
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

  closeModal() {
    this.isModalOpen = false;
  }

  saveCategoria() {
    if (this.selectedCategoria.id) {
      this.http.post(`http://localhost:8095/categoria/${this.selectedCategoria.id}`, this.selectedCategoria).subscribe(() => {
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

  deleteCategoria(id: number) {
    this.http.delete(`http://localhost:8095/categoria/${id}`).subscribe(() => {
      this.loadCategorias();
    });
  }
}
