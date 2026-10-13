# Бронирование переговорных комнат

REST-сервис для бронирования переговорных `ORION`, `VEGA`, `SIRIUS`.
У одной комнаты интервалы не пересекаются: касание границ (`endAt` одной = `startAt` другой) допустимо, пересечение → `409 Conflict`.

**Стек:** Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL 17, Liquibase, MapStruct, springdoc-openapi, JUnit 5 + Mockito.

## Запуск

Нужны JDK 21 и Docker.

```bash
cp .env.example .env          # логин/пароль БД для docker compose
docker compose up -d          # PostgreSQL на localhost:5432
./gradlew bootRun             # Windows: gradlew.bat bootRun
```

Таблицы создаёт Liquibase при старте приложения (`src/main/resources/db/changelog`).

По умолчанию приложение подключается к `jdbc:postgresql://localhost:5432/bookings` с `postgres` / `postgres`.
Если в `.env` указан другой пароль, передайте те же значения через переменные окружения `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

- API: http://localhost:8080/api/bookings
- Swagger UI: http://localhost:8080/swagger-ui.html

## Тесты

```bash
./gradlew test
```

- `BookingServiceImplTest` — unit-тесты сервиса (Mockito), в том числе на пересечение интервалов
- `BookingControllerTest` — `@WebMvcTest` контроллера: статусы, валидация, формат ошибок
- `TestovoeApplicationTests` — поднимает весь контекст, нужна запущенная БД

## API

| Метод | URL | Описание | Успех |
|---|---|---|---|
| POST | `/api/bookings` | создать бронь | 201 |
| GET | `/api/bookings/{id}` | получить по id | 200 |
| GET | `/api/bookings?room=VEGA&date=2030-12-01` | список, фильтры необязательные, сортировка по `startAt` | 200 |
| PUT | `/api/bookings/{id}` | изменить бронь | 200 |
| DELETE | `/api/bookings/{id}` | удалить бронь | 204 |

Фильтр `date` возвращает брони, которые пересекаются с этим днём.

## Примеры

Создать бронь:
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"room":"VEGA","title":"Daily standup","organizer":"Roman","startAt":"2030-12-01T10:00:00","endAt":"2030-12-01T11:00:00"}'
```

Список броней комнаты за день:
```bash
curl "http://localhost:8080/api/bookings?room=VEGA&date=2030-12-01"
```

Пересекающаяся бронь → `409`:
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"room":"VEGA","title":"Sync","organizer":"Anna","startAt":"2030-12-01T10:30:00","endAt":"2030-12-01T11:30:00"}'
```
```json
{"code":"BK0002","message":"Комната VEGA уже забронирована на это время","timestamp":"..."}
```

## Ошибки

Все ошибки возвращаются в одном формате: `code`, `message`, `timestamp`, при ошибках валидации ещё `details.fields`.
Тексты лежат в `messages.properties` и выбираются по коду ошибки.

| HTTP | Код | Когда |
|---|---|---|
| 400 | `VA0001` | не прошла валидация полей |
| 400 | `VA0002` | некорректный JSON или параметр (`room=MARS`, `date=01.12.2030`) |
| 400 | `BK0003` | `endAt` не позже `startAt` |
| 404 | `BK0001` | бронь не найдена |
| 409 | `BK0002` | пересечение с другой бронью этой комнаты |
