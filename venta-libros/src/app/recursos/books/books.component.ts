import { CommonModule, isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, Inject, OnInit, PLATFORM_ID } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Libro } from '../libro.model';
import { AuthService } from '../auth.service';
import { NotificationService } from '../notification/notification.service';

@Component({
  selector: 'app-books',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './books.component.html',
  styleUrl: './books.component.css'
})
export class BooksComponent implements OnInit {
  isCartOpen = false;
  cartItems: CartItem[] = [];
  books: Libro[] = [];
  categorias: any[] = [];
  anios: number[] = [];

  private readonly apiUrl = 'http://localhost:8095';

  constructor(
    private http: HttpClient,
    public authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    this.loadBooks();
    this.loadCategorias();
    this.loadAnios();

    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    if (this.getToken()) {
      this.loadCartItems();
    }
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

  loadBooks(): void {
    this.http.get<Libro[]>(`${this.apiUrl}/libros`).subscribe({
      next: (data: Libro[]) => {
        this.books = data;
      },
      error: (error: unknown) => {
        console.error('Error al obtener los libros:', error);
      }
    });
  }

  loadCategorias(): void {
    this.http.get<any[]>(`${this.apiUrl}/categoria`).subscribe({
      next: (data: any[]) => {
        this.categorias = data;
      },
      error: (error: unknown) => {
        console.error('Error al obtener las categorías:', error);
      }
    });
  }

  loadAnios(): void {
    this.anios = [2010, 2011, 2012, 2013, 2014, 2015, 2017, 2018, 2019, 2020];
  }

  filterByCategory(categoryId: number): void {
    this.http.get<Libro[]>(`${this.apiUrl}/libros/categoria/${categoryId}`).subscribe({
      next: (data: Libro[]) => {
        this.books = data;
      },
      error: (error: unknown) => {
        console.error('Error al obtener los libros por categoría:', error);
      }
    });
  }

  filterByYear(anio: number): void {
    this.http.get<Libro[]>(`${this.apiUrl}/libros/anio/${anio}`).subscribe({
      next: (data: Libro[]) => {
        this.books = data;
      },
      error: (error: unknown) => {
        console.error('Error al obtener los libros por año:', error);
      }
    });
  }

  getImageUrl(imagePath?: string): string {
    return imagePath
      ? `${this.apiUrl}/libros/uploads/${imagePath}`
      : 'ruta/por/defecto/imagen.png';
  }

  increaseQuantity(book: Libro): void {
    book.quantity = (book.quantity || 1) + 1;
  }

  decreaseQuantity(book: Libro): void {
    book.quantity = (book.quantity || 1) - 1;
    if ((book.quantity || 1) < 1) {
      book.quantity = 1;
    }
  }

  addToCart(book: Libro): void {
    const token = this.getToken();

    if (!token) {
      this.notificationService.showError('Debe iniciar sesión para agregar al carrito');
      this.router.navigate(['/login']);
      return;
    }

    const cartItem = {
      libroId: book.id,
      cantidad: book.quantity || 1
    };

    this.http.post(`${this.apiUrl}/carrito/agregar`, cartItem, {
      headers: this.getAuthHeaders()
    }).subscribe({
      next: () => {
        this.notificationService.showSuccess('Libro agregado al carrito con éxito');
        this.loadCartItems();
      },
      error: (error: { status?: number }) => {
        if (error.status === 401) {
          this.notificationService.showError('Debe iniciar sesión para agregar al carrito');
        } else {
          this.notificationService.showError('Error al agregar el libro al carrito');
        }
      }
    });
  }

  openCart(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadCartItems();
    this.isCartOpen = true;
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('token');
    }
    this.router.navigate(['/login']);
  }

  closeCart(): void {
    this.isCartOpen = false;
  }

  loadCartItems(): void {
    const token = this.getToken();

    if (!token) {
      console.log('No hay token para cargar el carrito');
      this.cartItems = [];
      return;
    }

    this.http.get<CartItem[]>(`${this.apiUrl}/carrito/listar`, {
      headers: this.getAuthHeaders()
    }).subscribe({
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
    this.cartItems.forEach(item => {
      this.http.get<Libro>(`${this.apiUrl}/libros/${item.libroId}`).subscribe({
        next: (bookDetails: Libro) => {
          item.image = bookDetails.imagenUrl;
          item.title = bookDetails.titulo;
          item.price = bookDetails.precio;
          item.description = bookDetails.autor;
        },
        error: (error: unknown) => {
          console.error('Error al obtener los detalles del libro:', error);
        }
      });
    });
  }

  removeFromCart(itemId: number): void {
    const token = this.getToken();

    if (!token) {
      this.notificationService.showError('Debe iniciar sesión para modificar el carrito');
      this.router.navigate(['/login']);
      return;
    }

    this.cartItems = this.cartItems.filter(item => item.id !== itemId);

    this.http.delete(`${this.apiUrl}/carrito/eliminar/${itemId}`, {
      headers: this.getAuthHeaders()
    }).subscribe({
      next: () => {
        this.notificationService.showSuccess('Libro eliminado del carrito con éxito');
        this.loadCartItems();
      },
      error: () => {
        this.notificationService.showError('Error al eliminar el libro del carrito');
      }
    });
  }

  getTotal(): number {
    return this.cartItems.reduce((total, item) => total + (item.price || 0) * item.cantidad, 0);
  }

  realizarCompra(): void {
    this.router.navigate(['/precompra']);
  }

  descargarRecibo(ventaId: number): void {
    const token = this.getToken();

    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    const reciboUrl = `${this.apiUrl}/venta/${ventaId}/recibo`;

    this.http.get(reciboUrl, {
      headers: this.getAuthHeaders(),
      responseType: 'blob'
    }).subscribe({
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
