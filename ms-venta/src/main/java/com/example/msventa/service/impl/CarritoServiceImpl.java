package com.example.msventa.service.impl;

import com.example.msventa.entity.CarritoItem;
import com.example.msventa.repository.CarritoItemRepository;
import com.example.msventa.service.CarritoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    private final CarritoItemRepository carritoItemRepository;

    public CarritoServiceImpl(CarritoItemRepository carritoItemRepository) {
        this.carritoItemRepository = carritoItemRepository;
    }

    @Override
    public CarritoItem agregarItem(Integer userId, Integer libroId, Integer cantidad) {
        CarritoItem item = new CarritoItem();
        item.setUserId(userId);
        item.setLibroId(libroId);
        item.setCantidad(cantidad);
        return carritoItemRepository.save(item);
    }

    @Override
    public void eliminarItem(Integer userId, Integer itemId) {
        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        if (!item.getUserId().equals(userId)) {
            throw new SecurityException("Acceso denegado");
        }

        carritoItemRepository.delete(item);
    }

    @Override
    public List<CarritoItem> listarItems(Integer userId) {
        return carritoItemRepository.findByUserId(userId);
    }
}