package com.example.order.controller;

import java.net.URI;
import java.util.List;

import com.example.order.dto.CreateOrderItemRequest;
import com.example.order.dto.OrderItemRequest;
import com.example.order.dto.OrderItemResponse;
import com.example.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order_items")
public class OrderItemController {
    private final OrderService orderService;

    public OrderItemController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderItemResponse> findAll() {
        return orderService.findAllItems();
    }

    @GetMapping("/{id}")
    public OrderItemResponse findById(@PathVariable Integer id) {
        return orderService.findItemById(id);
    }

    @PostMapping
    public ResponseEntity<OrderItemResponse> create(
            @Valid @RequestBody CreateOrderItemRequest request,
            HttpServletRequest httpRequest) {
        OrderItemResponse item = orderService.createItem(
                request, httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
        return ResponseEntity.created(URI.create("/order_items/" + item.id())).body(item);
    }

    @PutMapping("/{id}")
    public OrderItemResponse update(
            @PathVariable Integer id,
            @Valid @RequestBody OrderItemRequest request,
            HttpServletRequest httpRequest) {
        return orderService.updateItem(id, request, httpRequest.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        orderService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
