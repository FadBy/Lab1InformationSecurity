# Secure REST API

Защищенное REST API приложение на Java с использованием Spring Boot, PostgreSQL и интеграцией security-сканеров в CI/CD pipeline.

## Описание

Это учебный проект, демонстрирующий реализацию защищенного REST API с защитой от основных уязвимостей из OWASP Top 10:

- **SQL Injection** - защита через JPA/Hibernate (параметризованные запросы)
- **XSS (Cross-Site Scripting)** - санитизация пользовательского ввода
- **Broken Authentication** - JWT токены, хэширование паролей через bcrypt

## Технологический стек

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** - аутентификация и авторизация
- **PostgreSQL** - база данных
- **JWT (JSON Web Tokens)** - для аутентификации
- **BCrypt** - хэширование паролей
- **Maven** - управление зависимостями
- **JPA/Hibernate** - ORM для работы с БД

## Структура проекта

```
src/
├── main/
│   ├── java/com/infosec/secureapi/
│   │   ├── controller/      # REST контроллеры
│   │   ├── service/         # Бизнес-логика
│   │   ├── repository/      # JPA репозитории
│   │   ├── entity/          # Сущности БД
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── security/        # Security конфигурация
│   │   └── config/          # Конфигурационные классы
│   └── resources/
│       └── application.properties
└── test/
```

## API Эндпоинты

### 1. POST /auth/login
Аутентификация пользователя.

**Request:**
```json
{
  "username": "testuser",
  "password": "testpass123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "testuser"
}
```

### 2. GET /api/data
Получение списка всех данных. Требует аутентификации.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Example Title",
    "content": "Example Content",
    "username": "testuser",
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

### 3. POST /api/data
Создание новой записи. Требует аутентификации.

**Headers:**
```
Authorization: Bearer <token>
```

**Request:**
```json
{
  "title": "New Title",
  "content": "New Content"
}
```

**Response:**
```json
{
  "id": 2,
  "title": "New Title",
  "content": "New Content",
  "username": "testuser",
  "createdAt": "2024-01-01T12:00:00"
}
```

## Установка и запуск

### Требования

- Java 17 или выше
- Maven 3.6+
- PostgreSQL 12+

### Шаги установки

1. **Клонируйте репозиторий:**
```bash
git clone https://github.com/FadBy/Lab1InformationSecurity.git
cd Lab1InformationSecurity
```

2. **Создайте базу данных PostgreSQL:**
```sql
CREATE DATABASE infosec_db;
```

3. **Настройте переменные окружения:**

Создайте файл `.env` в корне проекта или установите переменные окружения:

```bash
export DB_NAME=infosec_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your-very-long-secret-key-minimum-256-bits-for-production
```

Или отредактируйте `src/main/resources/application.properties` напрямую.

4. **Соберите проект:**
```bash
mvn clean install
```

5. **Запустите приложение:**
```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

## Тестирование API

### Использование curl

1. **Аутентификация:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass123"}'
```

2. **Получение данных (с токеном):**
```bash
curl -X GET http://localhost:8080/api/data \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

3. **Создание данных:**
```bash
curl -X POST http://localhost:8080/api/data \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Title","content":"Test Content"}'
```

### Использование Postman

1. Импортируйте коллекцию или создайте запросы вручную
2. Для защищенных эндпоинтов добавьте заголовок:
   - Key: `Authorization`
   - Value: `Bearer <ваш_токен>`

## Меры безопасности

### 1. Защита от SQL Injection
- Использование JPA/Hibernate с параметризованными запросами
- Никакой конкатенации строк для SQL-запросов
- Все запросы проходят через ORM

### 2. Защита от XSS
- Санитизация всех пользовательских данных перед сохранением
- Экранирование HTML-символов (`<`, `>`, `&`, `"`, `'`, `/`)
- Валидация входных данных через Bean Validation

### 3. Защита от Broken Authentication
- Пароли хэшируются с помощью BCrypt (алгоритм с солью)
- JWT токены с истечением срока действия
- Middleware для проверки токенов на всех защищенных эндпоинтах
- Пароли никогда не передаются в открытом виде

## CI/CD Pipeline

Проект включает GitHub Actions workflow (`.github/workflows/ci.yml`) с автоматическими проверками:

### SAST (Static Application Security Testing)
- **SpotBugs** - статический анализ кода на потенциальные уязвимости
- Запускается при каждом push и pull request

### SCA (Software Composition Analysis)
- **OWASP Dependency-Check** - проверка зависимостей на известные уязвимости
- Генерирует отчеты в форматах HTML и JSON
- Автоматически комментирует PR при обнаружении уязвимостей

### Запуск проверок локально

**SpotBugs:**
```bash
mvn spotbugs:check
mvn spotbugs:spotbugs  # для генерации отчета
```

**OWASP Dependency-Check:**
```bash
mvn org.owasp:dependency-check-maven:check
```

Отчеты будут в папке `target/`:
- `target/spotbugsXml.xml` - отчет SpotBugs
- `target/dependency-check-report.html` - отчет OWASP Dependency-Check

## Тестовый пользователь

При первом запуске автоматически создается тестовый пользователь:
- **Username:** `testuser`
- **Password:** `testpass123`

⚠️ **Внимание:** В production окружении обязательно измените пароли и создайте собственных пользователей!

## Структура базы данных

### Таблица `users`
- `id` (BIGSERIAL PRIMARY KEY)
- `username` (VARCHAR(50) UNIQUE NOT NULL)
- `password` (VARCHAR NOT NULL) - хэшированный пароль
- `created_at` (TIMESTAMP)

### Таблица `data_items`
- `id` (BIGSERIAL PRIMARY KEY)
- `title` (VARCHAR(500) NOT NULL)
- `content` (VARCHAR(2000))
- `user_id` (BIGINT FOREIGN KEY REFERENCES users(id))
- `created_at` (TIMESTAMP)

## Разработка

### Добавление новых зависимостей
```bash
mvn dependency:tree  # просмотр дерева зависимостей
```

### Запуск тестов
```bash
mvn test
```

### Сборка JAR
```bash
mvn clean package
```

JAR файл будет в `target/secure-rest-api-1.0.0.jar`

## Лицензия

Этот проект создан в учебных целях.

## Автор

Создано в рамках лабораторной работы по Информационной безопасности.

