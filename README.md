# 📨 Notification Microservice (Event-Driven Architecture)

Microservicio especializado en la gestión asíncrona de notificaciones transaccionales. Diseñado para desacoplar el envío de correos electrónicos de la API principal, mejorando el rendimiento y la escalabilidad del sistema logístico.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-blue.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message%20Broker-FF6600.svg)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED.svg)

## 🚀 Arquitectura del Sistema

En arquitecturas monolíticas tradicionales, enviar un correo bloquea el hilo HTTP principal hasta que el servidor SMTP responde. Este microservicio resuelve ese problema implementando **Event-Driven Architecture (EDA)**.

1. La **API Principal** publica un evento (`OrderEventDTO`) en el *Exchange* de RabbitMQ y responde al usuario en milisegundos.
2. Este **Microservicio** escucha de forma pasiva la cola `order.notifications.queue`.
3. Al recibir un mensaje, inyecta los datos en una plantilla HTML dinámica usando **Thymeleaf** y envía el correo mediante `JavaMailSender`.

## 🛠️ Stack Tecnológico

- **Java 17** & **Spring Boot 3**
- **Spring AMQP**: Integración con el broker de RabbitMQ.
- **Spring Mail & JavaMailSender**: Protocolo SMTP para envío de emails.
- **Thymeleaf**: Motor de renderizado para plantillas de correo electrónico HTML responsivas.
- **Docker & Docker Compose**: Contenerización del entorno local de RabbitMQ.

## ⚙️ Cómo levantar el proyecto localmente

### 1. Iniciar el Broker de Mensajería
No necesitas instalar RabbitMQ en tu máquina. Utiliza el archivo `docker-compose.yml` incluido:

```bash
docker-compose up -d
```
Esto levantará el servidor AMQP en el puerto `5672` y la interfaz de gestión visual en `http://localhost:15672` (Credenciales: `guest` / `guest`).

### 2. Configurar SMTP (Opcional para pruebas)
Por defecto, la aplicación está preconfigurada en `application.yml` para conectarse a Mailtrap (un servidor SMTP seguro para desarrollo). Puedes usar tus credenciales gratuitas de Mailtrap o tu propio servidor SMTP corporativo.

### 3. Compilar y Ejecutar
Asegúrate de tener Maven instalado:
```bash
mvn clean install
mvn spring-boot:run
```

El servicio arrancará en el puerto `8081` y quedará a la espera de eventos en el background.
