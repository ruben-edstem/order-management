package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.model.Order;
import com.ecommerce.ordermanagement.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderEvent(Order order, String eventType) {
        OrderEvent event = new OrderEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setOrderId(order.getId());
        event.setCustomerEmail(order.getCustomerEmail());
        event.setCustomerName(order.getCustomerName());
        event.setStatus(order.getStatus().toString());
        event.setTotalAmount(order.getTotalAmount());
        event.setTimestamp(LocalDateTime.now());

        // Convert order items
        event.setItems(order.getItems().stream()
                .map(item -> new OrderEvent.OrderItemEvent(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList()));

        kafkaTemplate.send("order-events", event);
        log.info("Order event sent: {} for order {}", eventType, order.getId());
    }

    public void sendNotification(String type, String recipient, String message) {
        var notification = new NotificationEvent(
                UUID.randomUUID().toString(),
                type,
                recipient,
                message,
                LocalDateTime.now()
        );

        kafkaTemplate.send("notification-events", notification);
        log.info("Notification sent to {}: {}", recipient, message);
    }

    public void sendInventoryUpdate(Long productId, Integer quantity) {
        var inventoryEvent = new InventoryEvent(
                UUID.randomUUID().toString(),
                productId,
                quantity,
                LocalDateTime.now()
        );

        kafkaTemplate.send("inventory-events", inventoryEvent);
        log.info("Inventory update sent for product {}: {} units", productId, quantity);
    }

    // Event classes
    record NotificationEvent(String id, String type, String recipient, String message, LocalDateTime timestamp) {}
    record InventoryEvent(String id, Long productId, Integer quantityChange, LocalDateTime timestamp) {}
}