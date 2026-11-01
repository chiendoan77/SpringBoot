package com.example.order.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.order.client.ProductClient;
import com.example.order.client.ProductClient.Product;
import com.example.order.dto.CreateOrderItemRequest;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderItemRequest;
import com.example.order.dto.OrderItemResponse;
import com.example.order.dto.OrderResponse;
import com.example.order.dto.UpdateOrderStatusRequest;
import com.example.order.model.CustomerOrder;
import com.example.order.model.OrderItem;
import com.example.order.repository.CustomerOrderRepository;
import com.example.order.repository.OrderItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final ProductClient productClient;

    public OrderService(
            CustomerOrderRepository orderRepository,
            OrderItemRepository itemRepository,
            ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.productClient = productClient;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrderById(Integer id) {
        return toResponse(findOrder(id));
    }

    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request, String authorization) {
        Map<Integer, Product> products = findAvailableProducts(request.items(), authorization);
        CustomerOrder order = new CustomerOrder();
        order.setCustomerName(request.customerName().trim());
        order.setCustomerEmail(request.customerEmail().trim());
        for (OrderItemRequest requestItem : request.items()) {
            order.addItem(createItem(
                    products.get(requestItem.productId()), requestItem.quantity()));
        }
        updateTotalAmount(order);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Integer id, UpdateOrderStatusRequest request) {
        CustomerOrder order = findOrder(id);
        String status = request.status().trim().toLowerCase(Locale.ROOT);
        if (!List.of("pending", "completed", "cancelled").contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status must be pending, completed, or cancelled");
        }
        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Integer id) {
        orderRepository.delete(findOrder(id));
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> findAllItems() {
        return itemRepository.findAll().stream().map(this::toItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderItemResponse findItemById(Integer id) {
        return toItemResponse(findItem(id));
    }

    @Transactional
    public OrderItemResponse createItem(
            CreateOrderItemRequest request, String authorization) {
        CustomerOrder order = findOrder(request.orderId());
        OrderItem item = createItem(request.productId(), request.quantity(), authorization);
        order.addItem(item);
        itemRepository.save(item);
        updateTotalAmount(order);
        return toItemResponse(item);
    }

    @Transactional
    public OrderItemResponse updateItem(
            Integer id, OrderItemRequest request, String authorization) {
        OrderItem item = findItem(id);
        Product product = requireAvailableProduct(request.productId(), request.quantity(), authorization);
        applyProduct(item, product, request.quantity());
        updateTotalAmount(item.getOrder());
        return toItemResponse(item);
    }

    @Transactional
    public void deleteItem(Integer id) {
        OrderItem item = findItem(id);
        CustomerOrder order = item.getOrder();
        order.removeItem(item);
        updateTotalAmount(order);
    }

    private Map<Integer, Product> findAvailableProducts(
            List<OrderItemRequest> items, String authorization) {
        Map<Integer, Integer> quantitiesByProduct = new HashMap<>();
        for (OrderItemRequest item : items) {
            try {
                quantitiesByProduct.merge(item.productId(), item.quantity(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Combined quantity exceeds the supported maximum for product " + item.productId(),
                        exception);
            }
        }
        Map<Integer, Product> products = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : quantitiesByProduct.entrySet()) {
            products.put(
                    entry.getKey(),
                    requireAvailableProduct(entry.getKey(), entry.getValue(), authorization));
        }
        return products;
    }

    private OrderItem createItem(Integer productId, Integer quantity, String authorization) {
        Product product = requireAvailableProduct(productId, quantity, authorization);
        return createItem(product, quantity);
    }

    private OrderItem createItem(Product product, Integer quantity) {
        OrderItem item = new OrderItem();
        applyProduct(item, product, quantity);
        return item;
    }

    private Product requireAvailableProduct(
            Integer productId, Integer quantity, String authorization) {
        Product product = productClient.findById(productId, authorization);
        if (product.quantity() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient stock for product " + productId
                            + ": requested " + quantity + ", available " + product.quantity());
        }
        return product;
    }

    private void applyProduct(OrderItem item, Product product, Integer quantity) {
        item.setProductId(product.id());
        item.setProductName(product.name());
        item.setQuantity(quantity);
        item.setUnitPrice(product.price().setScale(2, RoundingMode.HALF_UP));
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP));
    }

    private void updateTotalAmount(CustomerOrder order) {
        BigDecimal total = order.getItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    }

    private CustomerOrder findOrder(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private OrderItem findItem(Integer id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getTotalAmount(),
                order.getStatus(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getOrder().getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice());
    }
}
