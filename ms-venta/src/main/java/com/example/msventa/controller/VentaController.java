package com.example.msventa.controller;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.service.PdfService;
import com.example.msventa.service.VentaService;
import com.itextpdf.text.DocumentException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venta")
public class VentaController {

    private final VentaService ventaService;
    private final PdfService pdfService;
    private final LibroFeign libroFeign;

    public VentaController(VentaService ventaService, PdfService pdfService, LibroFeign libroFeign) {
        this.ventaService = ventaService;
        this.pdfService = pdfService;
        this.libroFeign = libroFeign;
    }

    @PostMapping("/realizar")
    public ResponseEntity<Venta> realizarVenta(@RequestHeader("Authorization") String token) {
        Venta nuevaVenta = ventaService.realizarVenta(token);
        return ResponseEntity.ok(nuevaVenta);
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Venta>> listarVentas() {
        List<Venta> ventas = ventaService.listarVentas();

        for (Venta venta : ventas) {
            for (VentaDetalle detalle : venta.getDetalles()) {
                ResponseEntity<LibroDto> libroResponse = libroFeign.listarLibro(detalle.getLibroId());
                if (libroResponse.getStatusCode().is2xxSuccessful()) {
                    detalle.setLibro(libroResponse.getBody());
                }
            }
        }

        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/{id}/recibo")
    public ResponseEntity<byte[]> generarRecibo(@PathVariable Integer id) {
        Venta venta = ventaService.obtenerVentaPorId(id);
        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] pdfBytes = pdfService.generarReciboPdf(venta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "recibo_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/registroVentasPdf")
    public ResponseEntity<byte[]> generarRegistroVentasPdf() {
        List<Venta> ventas = ventaService.listarVentas();

        for (Venta venta : ventas) {
            for (VentaDetalle detalle : venta.getDetalles()) {
                ResponseEntity<LibroDto> libroResponse = libroFeign.listarLibro(detalle.getLibroId());
                if (libroResponse.getStatusCode().is2xxSuccessful()) {
                    detalle.setLibro(libroResponse.getBody());
                }
            }
        }

        try {
            byte[] pdfBytes = pdfService.generarRegistroVentasPdf(ventas);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "registro_ventas.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}