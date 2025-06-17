package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.model.Order;
import com.ecommerce.ordermanagement.model.OrderItem;
import com.ecommerce.ordermanagement.enums.OrderStatus;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final KafkaProducerService kafkaProducerService;

    public Order createOrder(Order order) {
        // Calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            var product = productService.getProductById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            total = total.add(item.getSubtotal());
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        // Send Kafka events
        kafkaProducerService.sendOrderEvent(savedOrder, "ORDER_CREATED");
        kafkaProducerService.sendNotification("EMAIL",
                savedOrder.getCustomerEmail(),
                "Your order #" + savedOrder.getId() + " has been received!");

        log.info("Order created: {}", savedOrder.getId());
        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        // Send appropriate event based on status
        String eventType = status == OrderStatus.CANCELLED ? "ORDER_CANCELLED" : "ORDER_UPDATED";
        kafkaProducerService.sendOrderEvent(updatedOrder, eventType);

        kafkaProducerService.sendNotification("EMAIL",
                updatedOrder.getCustomerEmail(),
                "Your order #" + orderId + " status updated to: " + status);

        return updatedOrder;
    }
}