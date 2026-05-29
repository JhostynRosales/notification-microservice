package com.jhostyn.notification.service;

import com.jhostyn.notification.dto.OrderEventDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendOrderConfirmationEmail(OrderEventDTO orderEvent) {
        log.info("Preparando correo de confirmación para el pedido: {}", orderEvent.getOrderId());
        
        try {
            Context context = new Context();
            context.setVariable("order", orderEvent);
            
            String htmlTemplate = templateEngine.process("order-confirmation", context);
            
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(orderEvent.getCustomerEmail());
            helper.setSubject("¡Tu pedido " + orderEvent.getOrderId() + " ha sido confirmado!");
            helper.setText(htmlTemplate, true);
            
            javaMailSender.send(mimeMessage);
            log.info("✅ Correo enviado exitosamente a: {}", orderEvent.getCustomerEmail());
            
        } catch (MessagingException e) {
            log.error("❌ Error al enviar el correo de confirmación: {}", e.getMessage());
            throw new RuntimeException("Error enviando correo", e);
        }
    }
}
