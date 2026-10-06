# CHANGELOG

## [Unreleased] — 2026-10-06

### Fixed

**Backend**

- NPE при сериализации уроков: `Map.of(...)` с null-значениями (`audioUrl`, `promptTranslation`, `explanation`, `nextLessonId`, …) → заменено на null-safe `LinkedHashMap` (`LessonController`, `QuizService`, `TelegramAuthService`, `ProgressService`)
- XP за правильный ответ теперь персистится в `user.xp` (раньше рос только от завершения урока); начисляется один раз за item (идемпотентность по существующим `answers`)
- `responseMs` больше не бросает `ClassCastException`, если ключ отсутствует (чтение через `Number`/`Long.parseLong`)
- Валидация доступности урока вынесена в `LessonService` и применяется в `GET /lessons/{id}`, `POST /start`, `POST /answer`, `POST /complete` (LOCKED → 403)
- Урок 1 теперь `AVAILABLE` для нового пользователя без записи прогресса — в списке уроков и в проверке доступности
- «Угадывание» курса через `findByCourseId(Long.MIN_VALUE)` → `CourseRepository.findByCode(nyutag.course-code)` (`LessonService`, `TelegramAuthService.getProfile`, `ProgressService`)
- Поиск предыдущего/следующего урока через PK `id ± 1` → по `(courseId, orderIndex)` (`LessonRepository.findByCourseIdAndOrderIndex`)
- `completeLesson` идемпотентен: повторное завершение не начисляет XP/streak; возвращает `xpEarned` и корректный `nextLessonId`
- `GET /api/me` возвращает `isNewUser` (нет прогресса и `last_activity_at == null`)
- Dev-auth в `TelegramAuthFilter` явно помечен как локальный, секреты не логируются

**Frontend**

- `QuizCard`: PHRASE/WORD — пассивная кнопка «Продолжить»/«Завершить» без ответа; после неверного ответа также показывается кнопка продолжения (не застреваем)
- `Lesson`: экран завершения показывает `+xpEarned` вместо NaN (`prevXp`), корректный переход к следующему уроку
- Онбординг: редирект `/` → `/onboarding` по `isNewUser`, старт с первого доступного урока; Home ведёт на реально доступный урок
- `CompleteResponse` дополнен `xpEarned`; `User` — `isNewUser`

### Added

- Frontend тесты: Vitest + Testing Library (jsdom), конфиг `vitest.config.ts`, setup `src/test/setup.ts`; тесты `QuizCard` и `Home`
- Backend тесты: `LessonServiceTest`, `LessonControllerTest`, `TelegramInitDataValidatorTest`; обновлён `QuizServiceTest` (XP/идемпотентность)

### Changed

- Тесты/README/CHANGELOG приведены в соответствие фактическому коду (ранее заявлялись несуществующие тесты и Vitest-конфиг)
- `docker-compose.yml` содержит dev-auth для локального запуска — **в production переопределить `NYUTAG_DEV_AUTH_ENABLED: "false"`**

---

## [Unreleased] — 2026-10-05

### Added

**Backend**

- Telegram HMAC-SHA256 авторизация (`TelegramAuthFilter`, `TelegramInitDataValidator`)
- Dev-авторизация через `NYUTAG_DEV_AUTH_ENABLED` + `NYUTAG_DEV_TELEGRAM_ID`
- 6 новых сущностей: `Course`, `Lesson`, `LessonItem`, `UserLessonProgress`, `Answer`, `ProgressStatus`, `LessonType`
- 6 новых репозиториев: `CourseRepository`, `LessonRepository`, `LessonItemRepository`, `ProgressRepository`, `AnswerRepository`
- 6 сервисов: `UserService`, `LessonService`, `ProgressService`, `QuizService`, `StreakService`, `TelegramAuthService`
- API endpoints: `/api/health`, `/api/me`, `/api/lessons`, `/api/lessons/{id}`, `/api/lessons/{id}/start`, `/api/lessons/{lessonId}/items/{itemId}/answer`, `/api/lessons/{id}/complete`, `/api/progress`, `/api/stats`
- GlobalExceptionHandler с кодированием ошибок
- NyutagProperties для конфигурации XP, dev-auth, CORS
- Seed контент: 1 курс A1, 12 уроков, ~60 items (все `needs_review = true`)
- Unit-тесты: `StreakServiceTest`, `QuizServiceTest`

**Frontend**

- React Router DOM с роутами `/`, `/lessons`, `/lesson/:id`, `/profile`
- API client с `X-Telegram-Init-Data` заголовком
- Hooks: `useUser`, `useLessons`, `useLesson`, `useCompleteLesson`, `useSubmitAnswer`, `useStats`, `useTelegram`
- 5 страниц: `Home`, `Lessons`, `Lesson`, `Profile`, `Onboarding`
- 8 компонентов: `BottomNav`, `ProgressBar`, `LessonCard`, `QuizCard`, `AudioButton`, `ResultFeedback`, `Skeleton`, `ErrorState`
- CSS variables для Telegram theme params (светлая/тёмная тема)
- `index.html` с `<meta viewport>` и `telegram-web-app.js`

**Infra**

- Dockerfile обновлён для Java 25 / Gradle 9.6
- Flyway миграции V2-V5
- `docker-compose.yml` с env vars для bot token и dev auth

### Changed

- `V1__init.sql` не изменён (уже применён в существующих БД)
- `LessonController` полностью переписан: вместо хардкода — сервисы → репозитории → PostgreSQL
- `TelegramAuthService.demoUser()` заменён на реальную initData валидацию
- `User` entity переведён на private поля + getters/setters
- CORS: origins из env переменных (без `*`)
- Frontend: жёстко заданный API URL заменён на `/api` с vite proxy

### Deprecated

- `@MockBean` в Spring Boot 3.5+ (заменён на `@Mock` с `lenient()`)

### Removed

- Хардкодные `List.of(...)` данные из `LessonController`
- `TelegramAuthService.demoUser()`
- Монолитный `main.tsx` (~29 строк)
- Монолитный `style.css` (единственная минифицированная строка)

### Breaking Changes

- Статусы уроков: `DONE/CURRENT/LOCKED` → `AVAILABLE/IN_PROGRESS/COMPLETED`
- `/api/lessons/{id}` больше не отдаёт `correctOptionIndex`/`correctAnswerText`
- `/api/lessons/{id}/complete` возвращает `{ xp, streak, nextLessonId, lessonCompleted }` вместо `{ ok, nextLesson, streak }`
- `GET /api/me` возвращает расширенный профиль с `lastName`, `avatarUrl`, `xp`, `progressPercent`
- Все запросы к `/api/**` требуют заголовок `X-Telegram-Init-Data` (кроме `/api/health`)