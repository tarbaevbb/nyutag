# Нютаг — Буряад хэлэн

Telegram Mini App для изучения бурятского языка. Реальная авторизация через Telegram, persisted прогресс/XP/streak, уроки как сущности БД, полноценный lesson flow.

## Оглавление

- [Архитектура](#архитектура)
- [Быстрый старт](#быстрый-старт)
- [Локальная разработка](#локальная-разработка)
- [Telegram Local Dev](#telegram-local-dev)
- [Backend API](#backend-api)
- [База данных](#база-данных)
- [Frontend](#frontend)
- [Конфигурация](#конфигурация)
- [Тесты](#тесты)
- [Roadmap](#roadmap)

---

## Архитектура

```
nyutag-mvp/
├── docker-compose.yml          # PostgreSQL + Backend + Frontend
├── backend/                    # Spring Boot 3.5.6 / Java 25
│   └── src/main/java/ru/nyutag/
│       ├── NyutagApplication.java
│       ├── common/             # Exceptions
│       ├── config/             # NyutagProperties, CorsConfig, FilterConfig
│       ├── course/             # Course entity + repository
│       ├── lesson/             # Lesson, LessonItem, LessonType + repositories
│       ├── progress/           # UserLessonProgress, ProgressStatus + repository
│       ├── quiz/               # Answer entity + repository
│       ├── service/            # Business logic services
│       ├── telegram/           # TelegramAuthFilter, TelegramInitDataValidator, TelegramUser
│       └── user/               # User entity, UserRepository, UserContext
├── frontend/                   # React 19.3 / Vite 8.3 / TypeScript 7.0
│   └── src/
│       ├── api/                # client.ts, types.ts
│       ├── hooks/              # useApi.ts, useTelegram.ts
│       ├── pages/              # Home, Lessons, Lesson, Profile, Onboarding
│       ├── components/         # BottomNav, QuizCard, AudioButton, etc.
│       ├── App.tsx
│       ├── main.tsx
│       └── style.css
└── content/                    # (empty) future audio/assets
```

**Поток данных:**

1. Пользователь открывает Mini App в Telegram.
2. Frontend передаёт `window.Telegram.WebApp.initData` в заголовке `X-Telegram-Init-Data`.
3. `TelegramAuthFilter` валидирует HMAC-SHA256 подпись, создаёт/upsert пользователя.
4. Все API запросы используют пользователя из контекста (ThreadLocal).
5. Прогресс, XP, streak сохраняются в PostgreSQL.

---

## Быстрый старт

### Запуск через Docker Compose

```bash
cd nyutag-mvp
docker compose up --build
```

Запустятся три сервиса:

| Сервис | Порт | URL |
|--------|------|-----|
| PostgreSQL | 5432 | `localhost:5432` |
| Backend | 8080 | `http://localhost:8080` |
| Frontend | 5173 | `http://localhost:5173` |

Проверка работоспособности:

```bash
curl http://localhost:8080/api/health
# {"status":"ok","app":"nyutag"}
```

---

## Локальная разработка

### PostgreSQL

```bash
docker compose up postgres
```

Параметры подключения:

| Параметр | Значение |
|----------|----------|
| Образ | `postgres:17-alpine` |
| База | `nyutag` |
| Пользователь | `nyutag` |
| Пароль | `nyutag` |

### Backend

```bash
cd backend
./gradlew bootRun
```

Backend запустится на `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend запустится на `http://localhost:5173` с проксированием `/api` на `http://localhost:8080`.

---

## Telegram Local Dev

Для локальной разработки Telegram Mini App нужен публичный URL. Используйте `cloudflared`:

```bash
cloudflared tunnel --url http://localhost:5173
```

Создайте бота через BotFather в Telegram, включите Mini App и укажите URL туннеля.

### Dev Auth (локальная разработка)

Для тестирования без реального Telegram включите dev-авторизацию:

```yaml
# application.yml
nyutag:
  dev-auth:
    enabled: true
    telegram-id: 123456789  # ваш Telegram ID
```

При `enabled: true` запросы без `X-Telegram-Init-Data` аутентифицируются как указанный dev-пользователь. **В production флаг должен быть `false`**.

---

## Backend API

Базовый URL: `/api`. Все запросы требуют заголовок `X-Telegram-Init-Data` (сырой initData из Telegram WebApp).

### GET /api/health

```json
{ "status": "ok", "app": "nyutag" }
```

### GET /api/me

Профиль текущего пользователя:

```json
{
  "id": 1,
  "firstName": "Баяр",
  "lastName": "Б",
  "username": "bayr",
  "avatarUrl": "https://...",
  "level": "BEGINNER",
  "xp": 150,
  "streak": 3,
  "currentLesson": 5,
  "progressPercent": 40,
  "isNewUser": false
}
```

`isNewUser` — `true`, если у пользователя нет ни одной записи прогресса и `last_activity_at == null`. Фронт использует его для редиректа `/` → `/onboarding`. Не полагаться на `xp > 0`.

### GET /api/lessons

Список уроков с статусом:

```json
[
  { "id": 1, "title": "Сайн байна!", "subtitle": "Приветствия", "orderIndex": 1, "xpReward": 20, "status": "COMPLETED" },
  { "id": 2, "title": "Танилсая!", "subtitle": "Знакомство", "orderIndex": 2, "xpReward": 20, "status": "IN_PROGRESS" },
  { "id": 3, "title": "Минии нэрэ...", "subtitle": "Меня зовут...", "orderIndex": 3, "xpReward": 20, "status": "AVAILABLE" },
  { "id": 4, "title": "Хаанаhаа ерэбэш?", "subtitle": "Откуда ты?", "orderIndex": 4, "xpReward": 20, "status": "LOCKED" }
]
```

Статусы: `LOCKED`, `AVAILABLE`, `IN_PROGRESS`, `COMPLETED`.

Правило доступности: урок с `orderIndex == 1` всегда `AVAILABLE` для нового пользователя без записи прогресса. Следующий урок становится `AVAILABLE` после `COMPLETED` предыдущего. Остальные — `LOCKED`. Та же проверка применяется к `GET /lessons/{id}`, `POST /start`, `POST /answer` и `POST /complete` (LOCKED → 403).

### GET /api/lessons/{id}

Детали урока (правильные ответы НЕ отдаются):

```json
{
  "id": 2,
  "title": "Танилсая!",
  "subtitle": "Знакомство",
  "items": [
    { "id": 1, "orderIndex": 1, "type": "PHRASE", "prompt": "Поздоровайтесь", "buryat": "Сайн байна!", "russian": "Здравствуйте!", "audioUrl": null },
    { "id": 2, "orderIndex": 2, "type": "MULTIPLE_CHOICE", "prompt": "Что означает «Сайн байна»?", "options": "[\"Спасибо\",\"Здравствуйте\",\"До свидания\"]" }
  ]
}
```

403 если урок `LOCKED`, 404 если не найден.

### POST /api/lessons/{id}/start

Начать урок. Возвращает прогресс и items.

### POST /api/lessons/{lessonId}/items/{itemId}/answer

```json
// Request
{ "selectedOptionIndex": 1 }
// или
{ "selectedText": "Correct answer" }

// Response
{
  "correct": true,
  "correctOptionIndex": 1,
  "correctAnswerText": "Здравствуйте",
  "explanation": "Универсальное приветствие.",
  "xpAwarded": 10
}
```

`xpAwarded` начисляется в `user.xp` только за **первый правильный ответ** на конкретный item (идемпотентность; повторные ответы дают 0 XP). Типы `PHRASE`/`WORD` — пассивное изучение, ответ не отправляется.

### POST /api/lessons/{id}/complete

Завершение урока. Награждает XP, обновляет streak, разблокирует следующий урок. Идемпотентно: повторный вызов не начисляет XP и streak ещё раз.

```json
{
  "xp": 170,
  "xpEarned": 20,
  "streak": 4,
  "nextLessonId": 3,
  "lessonCompleted": true
}
```

### GET /api/progress

Карта прогресса по урокам.

### GET /api/stats

Агрегатная статистика:

```json
{
  "xp": 150,
  "streak": 3,
  "completedLessons": 5,
  "correctPercent": 87
}
```

---

## База данных

Flyway автоматически применяет миграции. Не редактировать `V1__init.sql` — использовать новые версии.

### Таблицы

**users** (V1 + V2):

```sql
id BIGSERIAL PK,
telegram_id BIGINT NOT NULL UNIQUE,
username VARCHAR(255),
first_name VARCHAR(255),
last_name VARCHAR(255),
avatar_url VARCHAR(1024),
level VARCHAR(50) NOT NULL DEFAULT 'BEGINNER',
goal VARCHAR(255),
current_lesson INT NOT NULL DEFAULT 1,
streak INT NOT NULL DEFAULT 0,
xp INT NOT NULL DEFAULT 0,
last_activity_at TIMESTAMPTZ,
created_at TIMESTAMPTZ NOT NULL,
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

**courses** (V3):

```sql
id BIGSERIAL PK,
code VARCHAR(50) NOT NULL UNIQUE,
title VARCHAR(255) NOT NULL,
level VARCHAR(50) NOT NULL,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

**lessons** (V3):

```sql
id BIGSERIAL PK,
course_id BIGINT NOT NULL REFERENCES courses(id),
title VARCHAR(255) NOT NULL,
subtitle VARCHAR(255),
order_index INT NOT NULL,
level VARCHAR(50) NOT NULL,
xp_reward INT NOT NULL DEFAULT 20,
status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
UNIQUE(course_id, order_index)
```

**lesson_items** (V3):

```sql
id BIGSERIAL PK,
lesson_id BIGINT NOT NULL REFERENCES lessons(id),
order_index INT NOT NULL,
type VARCHAR(50) NOT NULL,  -- PHRASE, WORD, TRANSLATION, MULTIPLE_CHOICE, LISTENING
prompt TEXT,
prompt_translation TEXT,
buryat TEXT,
russian TEXT,
audio_url VARCHAR(1024),
options JSONB,
correct_option_index INT,
correct_answer_text TEXT,
explanation TEXT,
needs_review BOOLEAN NOT NULL DEFAULT false,  -- все seed-данные отмечены true
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

**user_lesson_progress** (V4):

```sql
id BIGSERIAL PK,
user_id BIGINT NOT NULL REFERENCES users(id),
lesson_id BIGINT NOT NULL REFERENCES lessons(id),
status VARCHAR(50) NOT NULL,  -- LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED
progress INT NOT NULL DEFAULT 0,
completed_at TIMESTAMPTZ,
attempts INT NOT NULL DEFAULT 0,
best_score INT,
xp_earned INT NOT NULL DEFAULT 0,
updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
UNIQUE(user_id, lesson_id)
```

**answers** (V4):

```sql
id BIGSERIAL PK,
user_id BIGINT NOT NULL REFERENCES users(id),
lesson_id BIGINT NOT NULL REFERENCES lessons(id),
lesson_item_id BIGINT NOT NULL REFERENCES lesson_items(id),
selected_option_index INT,
selected_text TEXT,
is_correct BOOLEAN NOT NULL,
response_ms INT,
answered_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

### Seed контент (V5)

1 курс A1 с 12 уроками (~60 items). Все items имеют `needs_review = true` — контент черновой и требует проверки носителем языка.

---

## Frontend

### Стек

| Технология | Версия |
|------------|--------|
| React | 19.3.0 |
| React DOM | 19.3.0 |
| React Router DOM | 6.30.0 |
| Vite | 8.3.2 |
| TypeScript | 7.0.2 |

### Структура

```
frontend/
├── Dockerfile
├── index.html              # Head, viewport, Telegram SDK script
├── package.json
├── tsconfig.json
├── vite.config.ts          # Proxy /api → localhost:8080
└── src/
    ├── api/
    │   ├── client.ts       # Fetch wrapper с X-Telegram-Init-Data
    │   └── types.ts        # DTO типы
    ├── hooks/
    │   ├── useApi.ts       # useUser, useLessons, useLesson, useComplete, useSubmit, useStats
    │   └── useTelegram.ts  # Telegram theme params → CSS variables
    ├── pages/
    │   ├── Home.tsx
    │   ├── Lessons.tsx
    │   ├── Lesson.tsx
    │   ├── Profile.tsx
    │   └── Onboarding.tsx
    ├── components/
    │   ├── BottomNav.tsx
    │   ├── ProgressBar.tsx
    │   ├── LessonCard.tsx
    │   ├── QuizCard.tsx
    │   ├── AudioButton.tsx
    │   ├── ResultFeedback.tsx
    │   ├── Skeleton.tsx
    │   └── ErrorState.tsx
    ├── App.tsx
    ├── main.tsx
    └── style.css           # CSS variables для темизации (светлая/тёмная)
```

### Роуты

| Путь | Страница |
|------|----------|
| `/` | Home — приветствие, продолжение |
| `/lessons` | Lessons — список всех уроков |
| `/lesson/:id` | Lesson — прохождение урока |
| `/profile` | Profile — XP, streak, статистика |
| `/onboarding` | Onboarding — первый запуск (редирект с `/` при `isNewUser`) |

### Telegram тема

CSS-переменные читаются из `window.Telegram.WebApp.themeParams`:

| CSS variable | Источник |
|-------------|----------|
| `--tg-bg-color` | `bg_color` |
| `--tg-text-color` | `text_color` |
| `--tg-button-color` | `button_color` |
| `--tg-hint-color` | `hint_color` |

---

## Конфигурация

### Backend (`application.yml`)

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/nyutag}
    username: ${SPRING_DATASOURCE_USERNAME:nyutag}
    password: ${SPRING_DATASOURCE_PASSWORD:nyutag}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true

nyutag:
  xp:
    correct-answer: 10
  lessons:
    completion-reward: 20
  course-code: BURYAT_A1
  auth-max-age-seconds: 86400
  dev-auth:
    enabled: ${NYUTAG_DEV_AUTH_ENABLED:false}
    telegram-id: ${NYUTAG_DEV_TELEGRAM_ID:1}
  telegram:
    bot-token: ${NYUTAG_TELEGRAM_BOT_TOKEN:}
  cors:
    allowed-origins: ${NYUTAG_CORS_ORIGINS:http://localhost:5173}
```

| Переменная | По умолчанию | Описание |
|------------|-------------|-----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/nyutag` | URL PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `nyutag` | Имя пользователя БД |
| `SPRING_DATASOURCE_PASSWORD` | `nyutag` | Пароль БД |
| `NYUTAG_TELEGRAM_BOT_TOKEN` | (пусто) | Токен бота для HMAC-валидации (только backend) |
| `NYUTAG_DEV_AUTH_ENABLED` | `false` | Включить dev-авторизацию (выключить в production) |
| `NYUTAG_DEV_TELEGRAM_ID` | `1` | ID dev-пользователя |
| `NYUTAG_CORS_ORIGINS` | `http://localhost:5173` | Разрешённые origins (без `*` в prod) |

### Docker Compose

```yaml
backend:
  environment:
    NYUTAG_TELEGRAM_BOT_TOKEN: ${NYUTAG_TELEGRAM_BOT_TOKEN:-}
    NYUTAG_DEV_AUTH_ENABLED: "false"
    NYUTAG_CORS_ORIGINS: "http://localhost:5173"
```

---

## Тесты

### Backend

```bash
cd backend
./gradlew test
```

- `StreakServiceTest` — логика streak (UTC день, increment, reset, multiple lessons same day)
- `QuizServiceTest` — правильный/неправильный ответ, XP за первое попадание и идемпотентность, отсутствие ответа, item не найден
- `LessonServiceTest` — доступность урока 1, блокировка/разблокировка, идемпотентность `completeLesson`
- `TelegramInitDataValidatorTest` — валидная/невалидная подпись, просроченный `auth_date`, отсутствующий hash
- `LessonControllerTest` — health, список уроков, null-safe `audioUrl`, недоступный урок, делегирование ответа

### Frontend

```bash
cd frontend
npm test
```

Vitest + Testing Library (jsdom) настроены (`vitest.config.ts`, `src/test/setup.ts`). Покрыты критичные потоки `QuizCard` (MC/TRANSLATION, неверный ответ → «Продолжить», PHRASE/WORD) и `Home` (редирект нового пользователя, «Продолжить»).

---

## Roadmap

### Выполнено (MVP)

- [x] Telegram HMAC-SHA256 авторизация (initData валидация)
- [x] Dev-авторизация для локальной разработки
- [x] PostgreSQL с Flyway миграциями (6 таблиц)
- [x] Seed контент: 1 курс, 12 уроков, ~60 items (все `needs_review = true`)
- [x] Прогресс/XP/streak с persisted хранением
- [x] Постепенная разблокировка уроков
- [x] Полноценный lesson flow на frontend (react-router-dom)
- [x] Telegram theme params → CSS variables
- [x] AudioButton (nullable audio_url)
- [x] Skeleton, ErrorState, ResultFeedback
- [x] Backend unit-тесты
- [x] Frontend тесты (Vitest + Testing Library, jsdom)

### В планах

- [ ] Реальные mp3 файлы в `frontend/public/audio/` (`audio_url` в seed = null)
- [ ] Интеграционные тесты с Testcontainers PostgreSQL
- [ ] Аудит и проверка seed контента носителем языка
- [ ] Production deployment docs

---

## Troubleshooting

| Проблема | Решение |
|----------|---------|
| `ddl-auto: validate` падает | Убедитесь, что все миграции применены |
| `401 Unauthorized` | Проверьте `X-Telegram-Init-Data` заголовок |
| Frontend не видит API | Убедитесь, что vite proxy настроен на `localhost:8080` |
| Telegram WebApp не работает | Убедитесь, что `telegram-web-app.js` загружен в `index.html` |
| `NYUTAG_TELEGRAM_BOT_TOKEN` пуст | Без токена Telegram auth не работает (кроме dev auth) |