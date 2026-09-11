# Анализ проекта FloodFill (GameStudio)

## 1. Что это за проект

Учебный проект по курсу GameStudio Технического университета Кошице (TUKE, `sk.tuke`).
Внутри шаблона GameStudio реализована игра **FloodFill** («Заливка»): игрок закрашивает
связанную область от левого верхнего угла `(0,0)`, цель — сделать всё поле
монохромным за ограниченное число ходов.

Репозиторий: `https://git.kpi.fei.tuke.sk/kp-labs/assignments-2026-virtual/kp5/gamestudio-11038.git`

> ⚠️ На момент анализа в рабочей папке все исходники были удалены (не закоммичено).
> По согласованию файлы восстановлены из git (`git restore .`) — 52 файла в `src/`.

---

## 2. Технологический стек

| Слой | Технология | Версия |
|---|---|---|
| Язык | Java | 18 (не LTS) |
| Платформа | Spring Boot | 3.2.2 |
| Веб | spring-boot-starter-web + **Thymeleaf** + jQuery (AJAX) | — |
| Данные | Spring Data JPA / Hibernate (EntityManager), драйвер PostgreSQL | 42.7.7 |
| Сборка | Maven, packaging `jar` | — |
| Консоль | JLine 4.0.0, ANSI-цвета | — |
| Тесты | JUnit 5 + JUnit 4 | `RELEASE` → исправлено на 5.10.2 / 4.13.2 |
| БД | PostgreSQL (localhost:5432/gamestudio) | — |

Запуск — **два независимых режима**:

1. **Веб-сервер** — `sk.tuke.gamestudio.server.GameStudioServer` (порт 8080, Thymeleaf UI + REST API).
2. **Консольный клиент** — `sk.tuke.gamestudio.SpringClient` → `Game.main`
   (`WebApplicationType.NONE`, `ConsoleUI`; сервисы работают через REST-клиенты на `http://localhost:8080`).

---

## 3. Архитектура

```
sk.tuke.gamestudio
├── SpringClient            # консольный запуск (REST-клиенты, ban server.* из скана)
├── entity                  # JPA-сущности: Score, Comment, Rating, User
├── game.floodfill
│   ├── core                # ЧИСТАЯ логика (без Spring): Field, Cell, ColorType, GameState
│   └── consoleui           # консольный UI (ANSI)
├── server
│   ├── GameStudioServer    # @SpringBootApplication, @Bean'ы JPA-сервисов
│   ├── controller          # UserController, FloodFillController (@Scope SESSION)
│   └── webservice          # REST: /api/score | comment | rating | user
└── service                 # интерфейсы + 3 реализации каждого сервиса:
    ├── *ServiceJDBC        #   «задание 1» — сырой JDBC (URL/креды захардкожены)
    ├── *ServiceJPA         #   «задание 2» — EntityManager + NamedQueries (используется сервером)
    └── *ServiceRestClient  #   REST-клиенты для консольного режима
```

Игровое ядро (`Field`):

- Поле 12–22 × 12–22; 7 цветов (`ColorType`).
- Лимит ходов: `round((rows + cols) / 2 * 2.2)`.
- Заливка — **рекурсивный flood-fill** по 4 связям от `(0,0)`.
- Победа (`SOLVED`) — все клетки одного цвета; поражение (`FAILED`) — ходы исчерпаны.
- Очки: `rows × cols − (секунды с начала игры)`, не ниже 0.
- **Undo/Redo** — снапшоты `ColorType[][]` в `ArrayDeque`, история до 50 ходов.
- `reset()` — восстановление изначальной раскраски (`initialColors`).

Проверка состояния `checkState()` — O(n²) на каждый ход; для поля ≤ 22×22 (484 клетки) это нормально.

---

## 4. REST API (проверяются в `gamestudio.http`)

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/score` | добавить/обновить очки (upsert по `game+player`) |
| GET | `/api/score/{game}` | топ-10 очков |
| POST | `/api/comment` | добавить комментарий |
| GET | `/api/comment/{game}` | последние 10 комментариев |
| POST | `/api/rating` | поставить оценку (1–5, upsert) |
| GET | `/api/rating/{game}` | средняя оценка |
| GET | `/api/rating/{game}/{player}` | оценка конкретного игрока |
| POST | `/api/user` | логин/авторегистрация |

Веб-часть (Thymeleaf, session-scoped `FloodFillController`):

- `/` — вход/регистрация (`UserController`), есть гостевой вход `player_XXXXX` (без сохранения очков);
- `/floodfill` — игровая страница; AJAX-фрагменты: `/floodfill/field?color=…`, `/undo`, `/redo`,
  `/newGame`, `/restartGame`, `/type?size=12..20`, `/rating`, `/comment`.

---

## 5. Слой данных

- Сущности: `Score` (уникальность `game+player`), `Comment`, `Rating`, `User` (таблица `users`).
- Запросы через `@NamedQuery` (JPQL), включая `LIMIT` — корректно.
- Схему создаёт Hibernate: `spring.jpa.hibernate.ddl-auto=update`.
- Пул HikariCP уже настроен скромно: `maximumPoolSize=2` — удачно для бесплатных облачных БД.
- Пароли: SHA-256 без соли (`PasswordUtils`). Регистрация совмещена с логином
  (`loginOrRegister`): если пользователя нет — создаётся.

---

## 6. Сильные стороны

1. **Чистое игровое ядро** — `core` не зависит от Spring/JPA, покрыто юнит-тестами (`FieldTest`).
2. Полный набор CRUD-сервисов в трёх исполнениях (JDBC / JPA / REST) — учебная цель проекта раскрыта.
3. Upsert очков и рейтинга (не дублируются записи игрока).
4. Undo/Redo с ограничением истории, сброс уровня к исходной раскраске.
5. Аккуратный session-scoped контроллер + AJAX-фрагменты Thymeleaf — UI без перезагрузки страницы.
6. Гостевой режим, «первая игра не сохраняет очки» (`scoreSaved`).
7. `hikari.maximumPoolSize=2` — уже готов к ограничениям облачных БД.

---

## 7. Проблемы и риски

### Критично для сборки/деплоя (исправлено в `pom.xml` в рамках этой работы)

| # | Проблема | Последствие | Статус |
|---|---|---|---|
| 1 | `maven.compiler.source/target = 1.18` — таких версий не существует | `mvn package` падает (в IntelliJ работает, т.к. IDE использует свой компилятор) | ✅ удалены; `java.version=18` прокидывается parent-pom |
| 2 | Нет `spring-boot-maven-plugin` | `mvn package` даёт «тонкий» jar без зависимостей, `java -jar` не стартует — **блокер любого деплоя** | ✅ добавлен, `mainClass=…GameStudioServer` |
| 3 | Версии JUnit `RELEASE` | Непредсказуемое разрешение версий, deprecated | ✅ зафиксированы 5.10.2 / 4.13.2 |

### Заметно, но не блокирует

- **SHA-256 без соли** для паролей — уязвимо к радужным таблицам; для продакшена нужен BCrypt (`spring-security-crypto`).
- `java.util.Date` вместо `java.time.*` — устаревший API.
- `Field` живёт **в памяти сессии**: при рестарте/засыпании бесплатного хостинга текущая партия теряется (таблицы очков в БД — сохраняются). Для учебного проекта — приемлемо; «взрослое» решение — сохранять состояние партии в БД.
- Рекурсивный flood-fill — при большом поле возможен `StackOverflow` (здесь максимум 22×22 — безопасно); идиоматичнее явный стек/очередь.
- Креды БД `postgres/postgres` захардкожены в `application.properties` и в `ScoreServiceJDBC` (мёртвый код в деплое, но источник путаницы).
- В `fillModel()` `canUndo/canRedo` сначала пишутся `false`, потом перезаписываются реальными значениями — лишний код.
- Ограничение длины имени расходится: веб — 14 символов, консоль — 9.
- CSRF-защиты нет (Spring Security не подключён) — для учебного проекта допустимо.
- Нет Maven Wrapper (`mvnw`) — CI/хостинг вынужден ставить Maven сам (в Dockerfile это учтено).
- `JLine 4.0.0` нужен только консоли; в веб-jar попадает «мёртвым» весом.

### Вывод о готовности

Проект **готов к деплою** после исправлений в `pom.xml` (уже внесены) и добавления
производственного профиля + Dockerfile (созданы, см. `DEPLOY_BESPLATNO.md`).
Блокеров в коде приложения для запуска на облачном хостинге нет.
