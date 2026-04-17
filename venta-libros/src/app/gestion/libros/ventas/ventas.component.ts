import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-ventas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ventas.component.html',
  styleUrl: './ventas.component.css'
})
export class VentasComponent implements OnInit {
  ventas: any[] = [];

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadVentas();
  }

  loadVentas(): void {
    this.http.get('http://localhost:8095/venta/listar').subscribe((data: any) => {
      this.ventas = data;
    });
  }

  downloadReceipt(id: number): void {
    const url = `http://localhost:8095/venta/${id}/recibo`;

    this.http.get(url, { responseType: 'blob' }).subscribe((blob: Blob) => {
      const downloadURL = globalThis.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadURL;
      link.download = `recibo_${id}.pdf`;
      link.click();
      globalThis.URL.revokeObjectURL(downloadURL);
    });
  }

  downloadSalesReport(): void {
    const url = 'http://localhost:8095/venta/registroVentasPdf';

    this.http.get(url, { responseType: 'blob' }).subscribe((blob: Blob) => {
      const downloadURL = globalThis.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadURL;
      link.download = 'registro_ventas.pdf';
      link.click();
      globalThis.URL.revokeObjectURL(downloadURL);
    });
  }
}
