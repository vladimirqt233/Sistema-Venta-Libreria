package com.example.msventa.service;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.LibroFeign;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    private final LibroFeign libroFeign;

    public PdfService(LibroFeign libroFeign) {
        this.libroFeign = libroFeign;
    }

    public byte[] generarReciboPdf(Venta venta) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        document.add(new Paragraph("Libros Ceace", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Paragraph("RUC: 12345678901", FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Recibo de Venta", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        document.add(new Paragraph(" "));

        PdfPTable tableInfo = new PdfPTable(2);
        tableInfo.setWidthPercentage(100);
        tableInfo.setSpacingBefore(10f);
        tableInfo.setSpacingAfter(10f);

        PdfPCell cell;
        cell = new PdfPCell(new Paragraph("ID Venta:"));
        cell.setBorder(Rectangle.NO_BORDER);
        tableInfo.addCell(cell);
        cell = new PdfPCell(new Paragraph(String.valueOf(venta.getId())));
        cell.setBorder(Rectangle.NO_BORDER);
        tableInfo.addCell(cell);

        cell = new PdfPCell(new Paragraph("Fecha:"));
        cell.setBorder(Rectangle.NO_BORDER);
        tableInfo.addCell(cell);
        cell = new PdfPCell(new Paragraph(String.valueOf(venta.getFecha())));
        cell.setBorder(Rectangle.NO_BORDER);
        tableInfo.addCell(cell);

        cell = new PdfPCell(new Paragraph("Usuario:"));
        cell.setBorder(Rectangle.NO_BORDER);
        tableInfo.addCell(cell);
        cell = new PdfPCell(new Paragraph(String.valueOf(venta.getUserName())));
        cell.setBorder(Rectangle.NO_BORDER);
        tableInfo.addCell(cell);

        document.add(tableInfo);

        PdfPTable tableDetalles = new PdfPTable(4);
        tableDetalles.setWidthPercentage(100);
        tableDetalles.setSpacingBefore(10f);
        tableDetalles.setSpacingAfter(10f);
        tableDetalles.setWidths(new float[]{1f, 4f, 1f, 2f});

        tableDetalles.addCell("Item");
        tableDetalles.addCell("Descripción");
        tableDetalles.addCell("Cantidad");
        tableDetalles.addCell("Precio");

        int itemCounter = 1;
        for (VentaDetalle detalle : venta.getDetalles()) {
            LibroDto libro = obtenerLibroDesdeDetalle(detalle);

            String titulo = libro.getTitulo();
            String autor = libro.getAutor();

            tableDetalles.addCell(String.valueOf(itemCounter++));
            tableDetalles.addCell(titulo + " - " + autor);
            tableDetalles.addCell(String.valueOf(detalle.getCantidad()));
            tableDetalles.addCell(String.valueOf(detalle.getPrecio()));
        }

        document.add(tableDetalles);

        PdfPTable tableTotales = new PdfPTable(2);
        tableTotales.setWidthPercentage(100);
        tableTotales.setSpacingBefore(10f);
        tableTotales.setSpacingAfter(10f);
        tableTotales.setWidths(new float[]{8f, 2f});

        cell = new PdfPCell(new Paragraph("Total:"));
        cell.setBorder(Rectangle.NO_BORDER);
        tableTotales.addCell(cell);
        cell = new PdfPCell(new Paragraph(String.format("%.2f", venta.getTotal())));
        cell.setBorder(Rectangle.NO_BORDER);
        tableTotales.addCell(cell);

        cell = new PdfPCell(new Paragraph("IGV:"));
        cell.setBorder(Rectangle.NO_BORDER);
        tableTotales.addCell(cell);
        cell = new PdfPCell(new Paragraph(String.format("%.2f", venta.getIgv())));
        cell.setBorder(Rectangle.NO_BORDER);
        tableTotales.addCell(cell);

        cell = new PdfPCell(new Paragraph("Total con IGV:"));
        cell.setBorder(Rectangle.NO_BORDER);
        tableTotales.addCell(cell);
        cell = new PdfPCell(new Paragraph(String.format("%.2f", venta.getTotalConIgv())));
        cell.setBorder(Rectangle.NO_BORDER);
        tableTotales.addCell(cell);

        document.add(tableTotales);

        document.close();
        return baos.toByteArray();
    }

    public byte[] generarRegistroVentasPdf(List<Venta> ventas) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        document.add(new Paragraph("Libros Ceace", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Paragraph("RUC: 12345678901", FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Registro de Ventas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        document.add(new Paragraph(" "));

        PdfPTable tableVentas = new PdfPTable(6);
        tableVentas.setWidthPercentage(100);
        tableVentas.setSpacingBefore(10f);
        tableVentas.setSpacingAfter(10f);
        tableVentas.setWidths(new float[]{1f, 2f, 2f, 2f, 2f, 2f});

        tableVentas.addCell("ID Venta");
        tableVentas.addCell("Fecha");
        tableVentas.addCell("Usuario ID");
        tableVentas.addCell("Total");
        tableVentas.addCell("IGV");
        tableVentas.addCell("Total con IGV");

        for (Venta venta : ventas) {
            tableVentas.addCell(String.valueOf(venta.getId()));
            tableVentas.addCell(String.valueOf(venta.getFecha()));
            tableVentas.addCell(String.valueOf(venta.getUserId()));
            tableVentas.addCell(String.format("%.2f", venta.getTotal()));
            tableVentas.addCell(String.format("%.2f", venta.getIgv()));
            tableVentas.addCell(String.format("%.2f", venta.getTotalConIgv()));

            PdfPTable tableDetalles = new PdfPTable(5);
            tableDetalles.setWidthPercentage(100);
            tableDetalles.setSpacingBefore(5f);
            tableDetalles.setWidths(new float[]{1f, 3f, 2f, 1f, 2f});

            tableDetalles.addCell("Item");
            tableDetalles.addCell("Descripción");
            tableDetalles.addCell("Cantidad");
            tableDetalles.addCell("Precio");
            tableDetalles.addCell("Precio con IGV");

            int itemCounter = 1;
            for (VentaDetalle detalle : venta.getDetalles()) {
                LibroDto libro = obtenerLibroDesdeDetalle(detalle);

                String titulo = libro.getTitulo();
                String autor = libro.getAutor();

                tableDetalles.addCell(String.valueOf(itemCounter++));
                tableDetalles.addCell(titulo + " - " + autor);
                tableDetalles.addCell(String.valueOf(detalle.getCantidad()));
                tableDetalles.addCell(String.format("%.2f", detalle.getPrecio()));
                tableDetalles.addCell(String.format("%.2f", detalle.getPrecio() * 1.18));
            }

            PdfPCell detallesCell = new PdfPCell(tableDetalles);
            detallesCell.setColspan(6);
            tableVentas.addCell(detallesCell);
        }

        document.add(tableVentas);
        document.close();
        return baos.toByteArray();
    }

    private LibroDto obtenerLibroDesdeDetalle(VentaDetalle detalle) {
        if (detalle.getLibro() != null) {
            return detalle.getLibro();
        }

        ResponseEntity<LibroDto> response = libroFeign.listarLibro(detalle.getLibroId());

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "No se pudo obtener información del libro con ID: " + detalle.getLibroId());
        }

        LibroDto libro = response.getBody();
        if (libro == null) {
            throw new IllegalStateException(
                    "La respuesta del libro vino vacía para el ID: " + detalle.getLibroId());
        }

        detalle.setLibro(libro);
        return libro;
    }
}