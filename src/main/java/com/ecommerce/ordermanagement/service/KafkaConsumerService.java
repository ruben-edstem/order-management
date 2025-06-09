package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final ProductService productService;

    @KafkaListener(topics = "order-events", groupId = "order-service")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received order event: {} for order {}", event.getEventType(), event.getOrderId());

        switch (event.getEventType()) {
            case "ORDER_CREATED":
                // Update inventory
                event.getItems().forEach(item -> {
                    productService.updateStock(item.getProductId(), -item.getQuantity());
                });
                log.info("Inventory updated for order {}", event.getOrderId());
                break;

            case "ORDER_CANCELLED":
                // Restore inventory
                event.getItems().forEach(item -> {
                    productService.updateStock(item.getProductId(), item.getQuantity());
                });
                log.info("Inventory restored for cancelled order {}", event.getOrderId());
                break;
        }
    }

    @KafkaListener(topics = "notification-events", groupId = "notification-service")
    public void handleNotification(KafkaProducerService.NotificationEvent notification) {
        log.info("Processing notification for {}: {}", notification.recipient(), notification.message());
        // In real app, send email/SMS here
    }

    @KafkaListener(topics = "inventory-events", groupId = "inventory-service")
    public void handleInventoryUpdate(KafkaProducerService.InventoryEvent event) {
        log.info("Inventory update received for product {}: {} units",
                event.productId(), event.quantityChange());
        // Additional inventory processing logic
    }
}