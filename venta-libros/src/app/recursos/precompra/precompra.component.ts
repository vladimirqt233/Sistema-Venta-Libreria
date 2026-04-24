import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-precompra',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './precompra.component.html',
  styleUrl: './precompra.component.css'
})
export class PrecompraComponent implements OnInit {
  cartItems: CartItem[] = [];
  regiones = ['Lima', 'Juliaca', 'Puno', 'Cusco', 'La Libertad'];
  distritos = ['Santa Anita', 'Comas', 'San Isidro'];
  selectedRegion = '';
  selectedDistrict = '';
  shippingCost = 0;
  subtotal = 0;

  private readonly apiUrl = 'http://localhost:8095';

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    const token = this.getToken();

    if (!token) {
      console.log('No hay token, redirigiendo al login');
      this.router.navigate(['/login']);
      return;
    }

    this.loadCartItems();
  }

  private getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    return localStorage.getItem('token');
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.getToken();

    return new HttpHeaders({
      Authorization: `Bearer ${token ?? ''}`
    });
  }

  loadCartItems(): void {
    const token = this.getToken();

    if (!token) {
      console.log('No hay token para cargar el carrito');
      return;
    }

    this.http
      .get<CartItem[]>(`${this.apiUrl}/carrito/listar`, {
        headers: this.getAuthHeaders()
      })
      .subscribe({
        next: (data: CartItem[]) => {
          this.cartItems = data ?? [];
          this.loadBookDetails();
        },
        error: (error: unknown) => {
          console.error('Error al obtener los items del carrito:', error);
        }
      });
  }

  loadBookDetails(): void {
    if (!this.cartItems.length) {
      this.calculateSubtotal();
      return;
    }

    this.cartItems.forEach((item) => {
      this.http.get<BookDetails>(`${this.apiUrl}/libros/${item.libroId}`).subscribe({
        next: (bookDetails: BookDetails) => {
          item.image = bookDetails.imagenUrl;
          item.title = bookDetails.titulo;
          item.price = bookDetails.precio;
          item.description = bookDetails.autor;
          this.calculateSubtotal();
        },
        error: (error: unknown) => {
          console.error('Error al obtener los detalles del libro:', error);
        }
      });
    });
  }

  calculateSubtotal(): void {
    this.subtotal = this.cartItems.reduce(
      (sum, item) => sum + (item.price ?? 0) * item.cantidad,
      0
    );
  }

  calculateShippingCost(): void {
    this.shippingCost = this.selectedRegion === 'Lima' ? 12 : 20;
  }

  finalizePurchase(): void {
    const token = this.getToken();

    if (!token) {
      console.log('No hay token para finalizar la compra');
      this.router.navigate(['/login']);
      return;
    }

    this.http
      .post<any>(
        `${this.apiUrl}/venta/realizar`,
        {},
        { headers: this.getAuthHeaders() }
      )
      .subscribe({
        next: (venta: any) => {
          console.log('Compra realizada con éxito:', venta);

          if (venta?.id) {
            this.descargarRecibo(venta.id);
          }

          this.router.navigate(['/']);
        },
        error: (error: unknown) => {
          console.error('Error al realizar la compra:', error);
        }
      });
  }

  descargarRecibo(ventaId: number): void {
    const token = this.getToken();

    if (!token) {
      console.log('No hay token para descargar el recibo');
      return;
    }

    const reciboUrl = `${this.apiUrl}/venta/${ventaId}/recibo`;

    this.http
      .get(reciboUrl, {
        headers: this.getAuthHeaders(),
        responseType: 'blob'
      })
      .subscribe({
        next: (response: Blob) => {
          if (!isPlatformBrowser(this.platformId)) {
            return;
          }

          const url = window.URL.createObjectURL(response);
          const link = document.createElement('a');
          link.href = url;
          link.download = `recibo_venta_${ventaId}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error: (error: unknown) => {
          console.error('Error al descargar el recibo:', error);
        }
      });
  }
}

interface CartItem {
  id: number;
  userId: number;
  libroId: number;
  cantidad: number;
  image?: string;
  title?: string;
  price?: number;
  description?: string;
}

interface BookDetails {
  imagenUrl?: string;
  titulo?: string;
  precio?: number;
  autor?: string;
}
