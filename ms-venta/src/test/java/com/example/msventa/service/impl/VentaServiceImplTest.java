package com.example.msventa.service.impl;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.CarritoItem;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.AuthFeign;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.repository.CarritoItemRepository;
import com.example.msventa.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private CarritoItemRepository carritoItemRepository;
    @Mock
    private AuthFeign authFeign;
    @Mock
    private LibroFeign libroFeign;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private CarritoItem item;
    private LibroDto libro;

    @BeforeEach
    void setUp() {
        item = new CarritoItem();
        item.setId(1);
        item.setUserId(10);
        item.setLibroId(100);
        item.setCantidad(2);

        libro = new LibroDto();
        libro.setId(100);
        libro.setTitulo("Clean Code");
        libro.setAutor("Robert C. Martin");
        libro.setStock(10);
        libro.setPrecio(50.0);
    }

    @Test
    @DisplayName("Debe realizar venta correctamente y limpiar carrito")
    void realizarVenta_debeGuardarVentaYVaciarCarrito() {
        when(authFeign.getUserId("Bearer token")).thenReturn(ResponseEntity.ok(10));
        when(authFeign.getUserName("Bearer token")).thenReturn(ResponseEntity.ok("Juan"));
        when(carritoItemRepository.findByUserId(10)).thenReturn(List.of(item));
        when(libroFeign.listarLibro(100)).thenReturn(ResponseEntity.ok(libro));
        when(libroFeign.actualizarStock(100, 8)).thenReturn(ResponseEntity.ok().build());
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta venta = invocation.getArgument(0);
            venta.setId(1);
            return venta;
        });

        Venta resultado = ventaService.realizarVenta("Bearer token");

        assertNotNull(resultado);
        assertEquals(10, resultado.getUserId());
        assertEquals("Juan", resultado.getUserName());
        assertEquals(100.0, resultado.getTotal());
        assertEquals(18.0, resultado.getIgv());
        assertEquals(118.0, resultado.getTotalConIgv());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals(100, resultado.getDetalles().get(0).getLibroId());
        verify(ventaRepository).save(any(Venta.class));
        verify(carritoItemRepository).deleteAll(List.of(item));
        verify(libroFeign).actualizarStock(100, 8);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando carrito está vacío")
    void realizarVenta_debeLanzarExcepcionCuandoCarritoVacio() {
        when(authFeign.getUserId(anyString())).thenReturn(ResponseEntity.ok(10));
        when(authFeign.getUserName(anyString())).thenReturn(ResponseEntity.ok("Juan"));
        when(carritoItemRepository.findByUserId(10)).thenReturn(new ArrayList<>());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.realizarVenta("Bearer token"));

        assertEquals("El carrito está vacío", ex.getMessage());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no hay stock suficiente")
    void realizarVenta_debeLanzarExcepcionCuandoStockInsuficiente() {
        libro.setStock(1);

        when(authFeign.getUserId(anyString())).thenReturn(ResponseEntity.ok(10));
        when(authFeign.getUserName(anyString())).thenReturn(ResponseEntity.ok("Juan"));
        when(carritoItemRepository.findByUserId(10)).thenReturn(List.of(item));
        when(libroFeign.listarLibro(100)).thenReturn(ResponseEntity.ok(libro));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.realizarVenta("Bearer token"));

        assertEquals("No hay suficiente stock para el libro: Clean Code", ex.getMessage());
        verify(ventaRepository, never()).save(any());
        verify(libroFeign, never()).actualizarStock(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no se puede obtener libro")
    void realizarVenta_debeLanzarExcepcionCuandoLibroNoDisponible() {
        when(authFeign.getUserId(anyString())).thenReturn(ResponseEntity.ok(10));
        when(authFeign.getUserName(anyString())).thenReturn(ResponseEntity.ok("Juan"));
        when(carritoItemRepository.findByUserId(10)).thenReturn(List.of(item));
        when(libroFeign.listarLibro(100)).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.realizarVenta("Bearer token"));

        assertEquals("No se pudo obtener información del libro con ID: 100", ex.getMessage());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe listar ventas")
    void listarVentas_debeRetornarLista() {
        Venta venta = new Venta();
        when(ventaRepository.findAll()).thenReturn(List.of(venta));

        List<Venta> resultado = ventaService.listarVentas();

        assertEquals(1, resultado.size());
        verify(ventaRepository).findAll();
    }

    @Test
    @DisplayName("Debe obtener venta por id cuando existe")
    void obtenerVentaPorId_debeRetornarVenta() {
        Venta venta = new Venta();
        venta.setId(1);
        when(ventaRepository.findById(1)).thenReturn(Optional.of(venta));

        Venta resultado = ventaService.obtenerVentaPorId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
    }

    @Test
    @DisplayName("Debe retornar null cuando venta no existe")
    void obtenerVentaPorId_debeRetornarNull() {
        when(ventaRepository.findById(99)).thenReturn(Optional.empty());

        Venta resultado = ventaService.obtenerVentaPorId(99);

        assertNull(resultado);
    }

    @Test
    @DisplayName("Debe obtener venta con detalles y enriquecer libros")
    void obtenerVentaConDetalles_debeCompletarInformacionLibro() {
        Venta venta = new Venta();
        VentaDetalle detalle = new VentaDetalle();
        detalle.setLibroId(100);
        detalle.setCantidad(2);
        detalle.setPrecio(50.0);
        detalle.setVenta(venta);
        venta.getDetalles().add(detalle);

        when(ventaRepository.findById(1)).thenReturn(Optional.of(venta));
        when(libroFeign.listarLibro(100)).thenReturn(ResponseEntity.ok(libro));

        Venta resultado = ventaService.obtenerVentaConDetalles(1);

        assertNotNull(resultado);
        assertEquals("Clean Code", resultado.getDetalles().get(0).getLibro().getTitulo());
        verify(libroFeign).listarLibro(100);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando venta no existe")
    void obtenerVentaConDetalles_debeLanzarExcepcionCuandoNoExisteVenta() {
        when(ventaRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.obtenerVentaConDetalles(99));

        assertEquals("Venta no encontrada", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando libro no se puede obtener al consultar detalle")
    void obtenerVentaConDetalles_debeLanzarExcepcionCuandoLibroNoDisponible() {
        Venta venta = new Venta();
        VentaDetalle detalle = new VentaDetalle();
        detalle.setLibroId(100);
        detalle.setVenta(venta);
        venta.getDetalles().add(detalle);

        when(ventaRepository.findById(1)).thenReturn(Optional.of(venta));
        when(libroFeign.listarLibro(100)).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.obtenerVentaConDetalles(1));

        assertEquals("No se pudo obtener información del libro con ID: 100", ex.getMessage());
    }
}