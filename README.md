# Notification System

A Spring Boot project I built to practice designing a notification service with multiple channels, user preferences, and delivery history.

The idea is simple: you send one API request, and the service checks the user's preferences, picks the right channel provider, and saves a history record for each channel you asked for.

There's also a small static frontend page to test the API from the browser, so I don't have to use Postman every time.

## Features

- Send notifications through `EMAIL`, `SMS`, `PUSH`, and `IN_APP`
- Create users with default notification preferences
- Fetch user details with their notification preferences
- Per-user channel preferences (user can opt out of a channel)
- Provider-based design, so adding a new channel later is easy
- Delivery history saved for every channel that gets processed
- Request validation with Jakarta Validation
- Basic exception handling for missing users, missing preferences, and missing providers
- Simple frontend page to send test notifications
- Unit tests for `NotificationService`
- MockMvc tests for `NotificationController`

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Jakarta Validation
- JUnit 5, Mockito, MockMvc
- Plain HTML, CSS, and JavaScript for the frontend

## How It Works

The request flow looks like this:

```text
POST /api/notifications
        |
        v
NotificationController
        |
        v
NotificationService
        |
        |-- loads the User
        |-- loads the UserPreference
        |-- goes through each requested channel
        |-- if the channel is enabled, sends it through the right provider
        |-- if the channel is disabled, skips it
        |-- saves a NotificationHistory row for every channel it processed
```

The controller doesn't do much on its own. It just takes the request and calls the service. All the real logic is in `NotificationService`.

The service goes through the requested channels one at a time. If a provider fails, that failure is caught and saved as a `FAILED` history row instead of crashing the request. So one bad channel doesn't stop the others from being processed.

### Provider design

Each channel has its own provider class that implements this interface:

```java
public interface NotificationProvider {
    Channel getChannel();
    void send(User user, String title, String body);
}
```

Current providers:

- `EmailNotificationProvider`
- `SmsNotificationProvider`
- `PushNotificationProvider`
- `InAppNotificationProvider`

`NotificationRouter` collects all the provider beans into a map, keyed by channel. When the service needs to send through a channel, it just asks the router for the right provider. This way the service doesn't need if/else chains for every channel.

**Note:** the providers don't actually send real emails, SMS, or push notifications right now. They just print to the console. Real integrations are not implemented yet, this is more about the design than the actual delivery.

### User preferences

Before sending anything, the service loads the user's preferences:

```java
userPreferenceRepository.findByUserId(user.getId())
```

For each requested channel:

- If it's enabled, the provider gets called.
- If it's disabled, the provider is skipped, and a history row is saved with status `SKIPPED` and the message `User has opted out of this channel`.

### Delivery statuses

Three possible statuses per channel:

- `SUCCESS` — provider ran without throwing an exception
- `FAILED` — provider threw an exception
- `SKIPPED` — user has that channel disabled

One `NotificationHistory` row is saved per requested channel. So if you send a request with `EMAIL`, `SMS`, and `PUSH`, you get three history rows, one for each.

## Database Schema

Using JPA entities with `spring.jpa.hibernate.ddl-auto=update`, so Hibernate manages the schema.

### users

| Field | Notes |
| --- | --- |
| `id` | Primary key |
| `name` | User name |
| `email` | Used by the email provider |
| `phone` | Used by the SMS provider |
| `pushToken` | Used by the push provider |

### user_preferences

One row per user.

| Field | Notes |
| --- | --- |
| `id` | Primary key |
| `user_id` | One-to-one with `users`, unique and required |
| `email_enabled` | Whether email is allowed |
| `sms_enabled` | Whether SMS is allowed |
| `push_enabled` | Whether push is allowed |
| `in_app_enabled` | Whether in-app is allowed |

All preference fields default to `true`.

### notification_history

| Field | Notes |
| --- | --- |
| `id` | Primary key |
| `user_id` | Many-to-one with `users` |
| `channel` | `EMAIL`, `SMS`, `PUSH`, or `IN_APP` |
| `title` | Notification title |
| `body` | Notification body |
| `status` | `SUCCESS`, `FAILED`, or `SKIPPED` |
| `errorMessage` | Error or skip reason, if there is one |
| `createdAt` | Set when the row is created |

## API

### Create User

```http
POST /api/users
Content-Type: application/json
```

Request body:

```json
{
  "name": "Rahul",
  "email": "rahul@example.com",
  "phone": "+919888888888",
  "pushToken": "test-push-token"
}
```

When a user is created, the app also creates default preferences:

```text
email = true
sms = true
push = true
inApp = true
```

Success response:

```json
{
  "userId": 1,
  "message": "User created successfully"
}
```

### Get User

```http
GET /api/users/{id}
```

Example:

```http
GET /api/users/1
```

Success response:

```json
{
  "id": 1,
  "name": "Rahul",
  "email": "rahul@example.com",
  "phone": "+919888888888",
  "pushToken": "test-push-token",
  "preferences": {
    "email": true,
    "sms": true,
    "push": true,
    "inApp": true
  }
}
```

If the user does not exist, it returns the same `404 Not Found` error format used by the notification API.

### Send Notification

```http
POST /api/notifications
Content-Type: application/json
```

Request body:

```json
{
  "userId": 1,
  "title": "Test Notification",
  "body": "Hello from frontend",
  "channels": ["EMAIL", "PUSH"]
}
```

Success response:

```http
200 OK
```

```text
Notification processed successfully
```

## Validation and Errors

The request DTO checks:

- create user fields: `name`, `email`, `phone`, and `pushToken`
- `userId` is required
- `title` is required, can't be blank
- `body` is required, can't be blank
- `channels` needs at least one value

Bad requests return `400 Bad Request` from Spring's validation.

`IllegalArgumentException` is caught by `GlobalExceptionHandler` and returned as `404 Not Found`. This covers cases like a missing user, missing preferences, or a missing provider.

Example error response:

```json
{
  "timestamp": "2026-08-14T15:33:46.088510",
  "status": 404,
  "error": "Not Found",
  "message": "User not found: 999"
}
```

## Frontend

Just one static page at:

```text
src/main/resources/static/index.html
```

It has:

- User ID input
- Title input
- Body textarea
- Checkboxes for each channel
- Submit button
- Success/error messages

It posts to `http://localhost:8080/api/notifications`. Open `http://localhost:8080/` after starting the app to use it.

## Running Locally

### Requirements

- Java 21
- PostgreSQL running locally
- A database called `notification_db`

Current local config:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/notification_db
spring.datasource.username=sumitnegi
spring.datasource.password=
server.port=8080
```

Change the username/password in `src/main/resources/application.properties` if yours is different.

### Start the app

```bash
./mvnw spring-boot:run
```

Backend and frontend both run on `http://localhost:8080`.

## Testing

Two test classes:

`NotificationServiceTest` and `UserServiceTest` (JUnit 5 + Mockito) cover:

- successful sends
- opted-out channels
- provider failures
- missing users
- multiple channels processed independently
- creating users with default preferences
- fetching users with preferences

`NotificationControllerTest` and `UserControllerTest` (Spring Boot Test + MockMvc) cover:

- valid requests
- validation failures
- error responses from `GlobalExceptionHandler`
- creating and fetching users

Run all tests:

```bash
./mvnw test
```

Run a single test class:

```bash
./mvnw -Dtest=NotificationServiceTest test
./mvnw -Dtest=NotificationControllerTest test
./mvnw -Dtest=UserServiceTest test
./mvnw -Dtest=UserControllerTest test
```

## Future Improvements

Things I want to add later, not done yet:

- Async notification processing
- Queue-based delivery (Kafka or RabbitMQ)
- Retry logic for failed providers
- Real email integration
- Real SMS integration
- Real push notification integration
- Authentication and authorization
- Rate limiting
- Better error response body
- Admin/user APIs to manage preferences
- API to fetch notification history
- More integration tests with a real test database
# notification-system
