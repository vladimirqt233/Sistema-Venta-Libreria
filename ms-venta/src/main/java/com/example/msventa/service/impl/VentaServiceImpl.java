package com.example.msventa.service.impl;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.CarritoItem;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.AuthFeign;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.repository.CarritoItemRepository;
import com.example.msventa.repository.VentaRepository;
import com.example.msventa.service.VentaService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    private static final double IGV = 0.18;

    private final VentaRepository ventaRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final AuthFeign authFeign;
    private final LibroFeign libroFeign;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            CarritoItemRepository carritoItemRepository,
            AuthFeign authFeign,
            LibroFeign libroFeign) {
        this.ventaRepository = ventaRepository;
        this.carritoItemRepository = carritoItemRepository;
        this.authFeign = authFeign;
        this.libroFeign = libroFeign;
    }

    @Override
    @Transactional
    public Venta realizarVenta(String token) {
        Integer userId = obtenerUserId(token);
        String userName = obtenerUserName(token);
        List<CarritoItem> items = carritoItemRepository.findByUserId(userId);

        if (items.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        Venta venta = new Venta();
        venta.setUserId(userId);
        venta.setUserName(userName);
        venta.setFecha(new Date());

        double total = 0.0;

        for (CarritoItem item : items) {
            LibroDto libro = obtenerLibroPorId(item.getLibroId());

            Integer stock = libro.getStock();
            Integer cantidad = item.getCantidad();

            if (stock == null || cantidad == null) {
                throw new IllegalStateException("Stock o cantidad inválida para el libro con ID: " + item.getLibroId());
            }

            if (stock < cantidad) {
                throw new IllegalStateException("No hay suficiente stock para el libro: " + libro.getTitulo());
            }

            VentaDetalle detalle = new VentaDetalle();
            detalle.setVenta(venta);
            detalle.setLibroId(libro.getId());
            detalle.setCantidad(cantidad);
            detalle.setPrecio(libro.getPrecio());
            detalle.setLibro(libro);
            venta.getDetalles().add(detalle);

            libroFeign.actualizarStock(libro.getId(), stock - cantidad);
            total += libro.getPrecio() * cantidad;
        }

        double igv = total * IGV;
        double totalConIgv = total + igv;

        venta.setTotal(total);
        venta.setIgv(igv);
        venta.setTotalConIgv(totalConIgv);

        Venta ventaGuardada = ventaRepository.save(venta);
        carritoItemRepository.deleteAll(items);

        return ventaGuardada;
    }

    @Override
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta obtenerVentaPorId(Integer id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta obtenerVentaConDetalles(Integer ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        for (VentaDetalle detalle : venta.getDetalles()) {
            LibroDto libro = obtenerLibroPorId(detalle.getLibroId());
            detalle.setLibro(libro);
        }

        return venta;
    }

    private Integer obtenerUserId(String token) {
        ResponseEntity<Integer> response = authFeign.getUserId(token);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("No se pudo obtener el userId desde el token");
        }

        Integer userId = response.getBody();
        if (userId == null) {
            throw new IllegalStateException("El userId recibido desde el token es nulo");
        }

        return userId;
    }

    private String obtenerUserName(String token) {
        ResponseEntity<String> response = authFeign.getUserName(token);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("No se pudo obtener el nombre del usuario desde el token");
        }

        String userName = response.getBody();
        if (userName == null || userName.isBlank()) {
            throw new IllegalStateException("El nombre del usuario recibido desde el token es inválido");
        }

        return userName;
    }

    private LibroDto obtenerLibroPorId(Integer libroId) {
        ResponseEntity<LibroDto> response = libroFeign.listarLibro(libroId);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("No se pudo obtener información del libro con ID: " + libroId);
        }

        LibroDto libro = response.getBody();
        if (libro == null) {
            throw new IllegalStateException("La respuesta del libro vino vacía para el ID: " + libroId);
        }

        return libro;
    }
}