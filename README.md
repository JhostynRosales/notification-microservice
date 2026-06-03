# Notification Microservice — Arquitectura Orientada a Eventos

*🇬🇧 [Read in English](./README_EN.md)*

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message%20Broker-FF6600.svg)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED.svg)

## Descripción

Microservicio independiente especializado en el envío asíncrono de notificaciones transaccionales. Diseñado para desacoplar la lógica de correos electrónicos de la API principal ([PetLogiLink-API](https://github.com/JhostynRosales/petlogilink-api)), eliminando el bloqueo del hilo HTTP y mejorando el rendimiento general del sistema.

## Problema que Resuelve

En arquitecturas monolíticas, cuando se procesa un pedido el servidor envía el correo directamente, bloqueando el hilo HTTP hasta que el servidor SMTP responde (entre 500ms y 3s). Esto degrada el tiempo de respuesta para el usuario final y reduce el throughput de la aplicación.

Este microservicio implementa **Event-Driven Architecture (EDA)** para resolver ese cuello de botella:

```
┌──────────────────┐          ┌─────────────┐          ┌─────────────────────┐
│   API Principal   │  ──────▶ │  RabbitMQ   │  ──────▶ │  Notification       │
│  (PetLogiLink)   │  publish  │  Exchange   │  consume │  Microservice       │
│                  │          │             │          │                     │
│  Responde al     │          │  Cola:      │          │  1. Lee el evento   │
│  cliente en      │          │  order.     │          │  2. Renderiza HTML  │
│  milisegundos    │          │  notifications│         │  3. Envía el correo │
└──────────────────┘          │  .queue     │          │     vía SMTP        │
                              └─────────────┘          └─────────────────────┘
```

## Estructura del Proyecto

```
notification-microservice/
├── docker-compose.yml                        # RabbitMQ + Management UI
├── pom.xml                                   # Maven (AMQP, Mail, Thymeleaf)
└── src/main/
    ├── java/com/jhostyn/notification/
    │   ├── NotificationApplication.java      # Punto de entrada
    │   ├── config/
    │   │   └── RabbitMQConfig.java            # Exchange, Queue, Bindings
    │   ├── consumer/
    │   │   └── NotificationListener.java      # @RabbitListener (consumidor)
    │   ├── dto/
    │   │   └── OrderEventDTO.java             # Estructura del evento
    │   └── service/
    │       └── EmailService.java              # Thymeleaf + JavaMailSender
    └── resources/
        ├── application.yml                    # Configuración AMQP y SMTP
        └── templates/
            └── order-confirmation.html        # Plantilla HTML del correo
```

## Stack Tecnológico

| Componente | Tecnología | Propósito |
|------------|------------|-----------|
| Runtime | Java 17, Spring Boot 3 | Framework principal |
| Mensajería | Spring AMQP, RabbitMQ | Broker de eventos asíncronos |
| Email | Spring Mail, JavaMailSender | Protocolo SMTP |
| Plantillas | Thymeleaf | Renderizado de correos HTML |
| Infraestructura | Docker, Docker Compose | Contenerización del broker |

## Flujo de Procesamiento

1. La API principal recibe un pedido y publica un `OrderEventDTO` en el Exchange `petlogilink.exchange` con la routing key `order.created`.
2. RabbitMQ enruta el mensaje a la cola `order.notifications.queue`.
3. El `NotificationListener` consume el evento de forma asíncrona con `@RabbitListener`.
4. El `EmailService` inyecta los datos del pedido en la plantilla Thymeleaf `order-confirmation.html`.
5. El correo HTML se envía al cliente mediante `JavaMailSender`.

## Instalación

### Prerrequisitos
- Java JDK 17+
- Maven
- Docker (para levantar RabbitMQ)

### 1. Iniciar RabbitMQ

```bash
docker-compose up -d
```

El broker AMQP arrancará en el puerto `5672`. La interfaz de gestión visual estará disponible en `http://localhost:15672` (credenciales: `guest` / `guest`).

### 2. Configurar SMTP

El proyecto viene preconfigurado para [Mailtrap](https://mailtrap.io/) (servidor SMTP seguro para desarrollo). Edita `application.yml` con tus credenciales de Mailtrap o tu propio servidor SMTP:

```yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: tu_usuario_mailtrap
    password: tu_password_mailtrap
```

### 3. Compilar y Ejecutar

```bash
mvn clean install
mvn spring-boot:run
```

El servicio arrancará en el puerto `8081` y quedará escuchando eventos en la cola de RabbitMQ.

## Configuración de RabbitMQ

| Parámetro | Valor |
|-----------|-------|
| Exchange | `petlogilink.exchange` (Topic) |
| Queue | `order.notifications.queue` (Durable) |
| Routing Key | `order.created` |
| Serialización | JSON (Jackson2JsonMessageConverter) |

## Nota sobre Seguridad

- Las credenciales SMTP son valores de ejemplo (`mock_user_123`). En un entorno real se externalizan mediante variables de entorno o un servicio de secretos (Vault, AWS Secrets Manager).
- La conexión a RabbitMQ utiliza las credenciales por defecto de Docker (`guest/guest`), adecuadas para desarrollo local.

## Proyectos Relacionados

| Repositorio | Descripción |
|-------------|-------------|
| [petlogilink-api](https://github.com/JhostynRosales/petlogilink-api) | API REST + Dashboard Angular — Plataforma principal de logística |
