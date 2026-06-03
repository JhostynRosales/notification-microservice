# Notification Microservice — Event-Driven Architecture

*🇪🇸 [Leer en Español](./README.md)*

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message%20Broker-FF6600.svg)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED.svg)

## Description

Standalone microservice specialized in asynchronous transactional email delivery. Designed to decouple email logic from the main API ([PetLogiLink-API](https://github.com/JhostynRosales/petlogilink-api)), eliminating HTTP thread blocking and improving overall system performance.

## Problem It Solves

In monolithic architectures, when an order is processed the server sends the email directly, blocking the HTTP thread until the SMTP server responds (between 500ms and 3s). This degrades response time for the end user and reduces application throughput.

This microservice implements **Event-Driven Architecture (EDA)** to resolve that bottleneck:

```
┌──────────────────┐          ┌─────────────┐          ┌─────────────────────┐
│    Main API       │  ──────▶ │  RabbitMQ   │  ──────▶ │  Notification       │
│  (PetLogiLink)   │  publish  │  Exchange   │  consume │  Microservice       │
│                  │          │             │          │                     │
│  Responds to     │          │  Queue:     │          │  1. Reads the event │
│  the client in   │          │  order.     │          │  2. Renders HTML    │
│  milliseconds    │          │  notifications│         │  3. Sends email     │
└──────────────────┘          │  .queue     │          │     via SMTP        │
                              └─────────────┘          └─────────────────────┘
```

## Project Structure

```
notification-microservice/
├── docker-compose.yml                        # RabbitMQ + Management UI
├── pom.xml                                   # Maven (AMQP, Mail, Thymeleaf)
└── src/main/
    ├── java/com/jhostyn/notification/
    │   ├── NotificationApplication.java      # Entry point
    │   ├── config/
    │   │   └── RabbitMQConfig.java            # Exchange, Queue, Bindings
    │   ├── consumer/
    │   │   └── NotificationListener.java      # @RabbitListener (consumer)
    │   ├── dto/
    │   │   └── OrderEventDTO.java             # Event structure
    │   └── service/
    │       └── EmailService.java              # Thymeleaf + JavaMailSender
    └── resources/
        ├── application.yml                    # AMQP and SMTP configuration
        └── templates/
            └── order-confirmation.html        # HTML email template
```

## Tech Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| Runtime | Java 17, Spring Boot 3 | Core framework |
| Messaging | Spring AMQP, RabbitMQ | Asynchronous event broker |
| Email | Spring Mail, JavaMailSender | SMTP protocol |
| Templates | Thymeleaf | HTML email rendering |
| Infrastructure | Docker, Docker Compose | Broker containerization |

## Processing Flow

1. The main API receives an order and publishes an `OrderEventDTO` to the `petlogilink.exchange` Exchange with routing key `order.created`.
2. RabbitMQ routes the message to the `order.notifications.queue` queue.
3. The `NotificationListener` consumes the event asynchronously via `@RabbitListener`.
4. The `EmailService` injects the order data into the Thymeleaf template `order-confirmation.html`.
5. The HTML email is sent to the customer via `JavaMailSender`.

## Getting Started

### Prerequisites
- Java JDK 17+
- Maven
- Docker (to run RabbitMQ)

### 1. Start RabbitMQ

```bash
docker-compose up -d
```

The AMQP broker will start on port `5672`. The management UI will be available at `http://localhost:15672` (credentials: `guest` / `guest`).

### 2. Configure SMTP

The project comes preconfigured for [Mailtrap](https://mailtrap.io/) (a secure SMTP server for development). Edit `application.yml` with your Mailtrap credentials or your own SMTP server:

```yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: your_mailtrap_user
    password: your_mailtrap_password
```

### 3. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The service will start on port `8081` and will listen for events on the RabbitMQ queue.

## RabbitMQ Configuration

| Parameter | Value |
|-----------|-------|
| Exchange | `petlogilink.exchange` (Topic) |
| Queue | `order.notifications.queue` (Durable) |
| Routing Key | `order.created` |
| Serialization | JSON (Jackson2JsonMessageConverter) |

## Security Notice

- SMTP credentials are placeholder values (`mock_user_123`). In a production environment, these are externalized via environment variables or a secrets manager (Vault, AWS Secrets Manager).
- The RabbitMQ connection uses Docker's default credentials (`guest/guest`), suitable for local development only.

## Related Projects

| Repository | Description |
|------------|-------------|
| [petlogilink-api](https://github.com/JhostynRosales/petlogilink-api) | REST API + Angular Dashboard — Main logistics platform |
