package com.example.msventa.controller;

import com.example.msventa.dto.CarritoItemRequestDto;
import com.example.msventa.entity.CarritoItem;
import com.example.msventa.exception.TokenAuthenticationException;
import com.example.msventa.feign.AuthFeign;
import com.example.msventa.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final AuthFeign authFeign;

    public CarritoController(CarritoService carritoService, AuthFeign authFeign) {
        this.carritoService = carritoService;
        this.authFeign = authFeign;
    }

    @PostMapping("/agregar")
    public ResponseEntity<CarritoItem> agregarItem(
            @RequestHeader("Authorization") String token,
            @RequestBody CarritoItemRequestDto item) {

        Integer userId = obtenerUserIdDesdeToken(token);
        CarritoItem nuevoItem = carritoService.agregarItem(userId, item.getLibroId(), item.getCantidad());
        return ResponseEntity.ok(nuevoItem);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarItem(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id) {

        Integer userId = obtenerUserIdDesdeToken(token);
        carritoService.eliminarItem(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listar")
    public ResponseEntity<List<CarritoItem>> listarItems(
            @RequestHeader("Authorization") String token) {

        Integer userId = obtenerUserIdDesdeToken(token);
        List<CarritoItem> items = carritoService.listarItems(userId);
        return ResponseEntity.ok(items);
    }

    private Integer obtenerUserIdDesdeToken(String token) {
        ResponseEntity<Integer> response = authFeign.getUserId(token);

        if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new TokenAuthenticationException("No se pudo obtener el userId desde el token.");
        }

        return response.getBody();
    }
}