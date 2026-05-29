package com.jhostyn.notification.consumer;

import com.jhostyn.notification.dto.OrderEventDTO;
import com.jhostyn.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final EmailService emailService;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void consumeOrderCreatedEvent(OrderEventDTO orderEvent) {
        log.info("📥 Evento recibido desde RabbitMQ para el Pedido: {}", orderEvent.getOrderId());
        
        try {
            // Se simula un ligero retraso de procesamiento para demostrar asincronismo
            Thread.sleep(1000);
            emailService.sendOrderConfirmationEmail(orderEvent);
        } catch (Exception e) {
            log.error("❌ Error procesando el evento del pedido {}: {}", orderEvent.getOrderId(), e.getMessage());
            // En un sistema real aquí se enviaría a una Dead Letter Queue (DLQ)
        }
    }
}
