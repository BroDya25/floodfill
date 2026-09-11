# Бесплатный деплой FloodFill: Render + Neon (пошагово)

## 1. Почему именно эта связка

Игра — это **Spring Boot (Java) + PostgreSQL**, значит нужен бесплатный хостинг,
который умеет: (а) запускать Java/контейнеры, (б) держать постоянно доступный PostgreSQL.

Состояние рынка бесплатных тарифов (проверено по официальной документации):

| Хостинг | Java | Free-тариф | PostgreSQL | Вердикт |
|---|---|---|---|---|
| **Render** — Web Service (Docker) | ✅ через Docker | ✅ навсегда: 750 инстанс-часов/мес, без карты; засыпает после 15 мин простоя, подъём ~1 мин | ⚠️ свой бесплатный Postgres **удаляется через 30 дней** | ✅ **берём для приложения** |
| **Neon** (Postgres) | — | ✅ навсегда, без карты: 0.5 ГБ, 100 CU-ч/мес, scale-to-zero (будится запросом за ~0.5 с), **не удаляется** | ✅ нативный Postgres | ✅ **берём для БД** |
| Supabase | — | ✅ 500 МБ, но проект **замораживается после недели неактивности** | ✅ Postgres | 🟡 запасной вариант БД |
| Railway | ✅ | ❌ только разовый кредит $5 | — | ❌ |
| Koyeb | ✅ | ❌ free-тариф убран (Pro от $29; остался лишь dev-Postgres 5 ч/день) | — | ❌ |
| Fly.io | ✅ | ❌ для новых аккаунтов платно (pay-as-you-go) | — | ❌ |
| Heroku | ✅ | ❌ free отменён давно | — | ❌ |
| Render Postgres free | — | — | ⚠️ 1 ГБ, но **30 дней** | ❌ как постоянная БД |
| Google Cloud Run | ✅ | free-уровень есть, но требует **подключённой карты** | внешний | 🟡 сложнее, как запасной путь |
| Azure App Service F1 | ✅ (нативно, без Docker) | ✅ F1: 1 ГБ RAM, квота CPU 60 мин/день | Azure PG Flexible бесплатно 12 мес | 🟡 запасной вариант |

**Итог: Render (приложение, Docker) + Neon (PostgreSQL) = 0 ₽/мес навсегда, без карты.**

Что уже подготовлено в проекте для этого:

- `pom.xml` — исправлен (добавлен `spring-boot-maven-plugin`, убраны невалидные `1.18`, зафиксированы JUnit) — без этого jar не соберётся;
- `Dockerfile` — двухэтапная сборка Maven→JRE, слушает порт из `$PORT`;
- `render.yaml` — Blueprint для Render (профиль `prod`, регион Frankfurt);
- `src/main/resources/application-prod.properties` — prod-профиль: строка БД из переменной `JDBC_DATABASE_URL`, пул 2 соединения, порт из `$PORT`.

---

## 2. Шаг 1 — создать PostgreSQL на Neon (5 минут)

1. Зарегистрироваться на <https://neon.tech> (можно через GitHub; **карта не нужна**).
2. Создать проект, например `floodfill` (регион ближе к вам, например `EU Central-1 (Frankfurt)`).
3. На дашборде проекта скопировать **Connection string** вида:
   ```
   postgresql://neondb_owner:XXXX@ep-cool-name-123456.eu-central-1.aws.neon.tech/neondb
   ```
   > Совет: берите **direct endpoint** (без `-pooler` в имени хоста) — с пулом Hikari 2 соединения
   > PgBouncer не нужен. SSL включите параметром `sslmode=require`.
4. Преобразовать в JDBC-строку — она понадобится на шаге 3:
   ```
   jdbc:postgresql://ep-cool-name-123456.eu-central-1.aws.neon.tech/neondb?user=neondb_owner&password=XXXX&sslmode=require
   ```

Таблицы (`score`, `users`, `comment`, `rating`) создаст Hibernate сам (`ddl-auto=update`) при первом старте.

---

## 3. Шаг 2 — выложить код туда, откуда умеет забирать Render

Render подключает **GitHub / GitLab.com / Bitbucket**. Университетский GitLab
(`git.kpi.fei.tuke.sk`) — self-hosted, напрямую Render его не принимает.

Проще всего — бесплатный репозиторий на GitHub:

```bash
# из папки проекта (все исходники уже восстановлены из git)
git remote add github https://github.com/<ваш-ник>/floodfill.git
git push -u github master
```

(Либо создайте пустой репозиторий на GitHub и запушьте туда; коммит с нашими
исправлениями: `git add -A && git commit -m "Deploy: pom fix, Dockerfile, prod profile, render.yaml"`.)

> Альтернатива без GitHub: завести аккаунт на **gitlab.com** и запушить туда же.

---

## 4. Шаг 3 — создать сервис на Render (5–10 минут)

1. Зарегистрироваться на <https://render.com> (можно через GitHub; **карта не нужна**).
2. **New → Web Service**, выбрать репозиторий из шага 2.
3. Настройки:
   - **Runtime:** `Docker` (Java у Render не входит в нативные рантаймы — только Docker-образ);
   - **Instance Type:** `Free` (0.1 CPU / 512 MB);
   - **Region:** `Frankfurt`;
   - **Environment Variables:**
     | Key | Value |
     |---|---|
     | `SPRING_PROFILES_ACTIVE` | `prod` |
     | `JDBC_DATABASE_URL` | JDBC-строка из шага 1 |
4. **Create Web Service.** Первая сборка займёт ~5–10 минут (Maven в Docker).

Готово: игра доступна по адресу вида `https://floodfill-game.onrender.com`
(логин/регистрация → игра, топ-очков, комментарии и рейтинги — уже в облачной БД).

> Если не хотите кликать руками: после пуша в Render Dashboard → **New → Blueprint**
> — Render прочитает `render.yaml` и сам предложит конфигурацию, спросив только
> секретный `JDBC_DATABASE_URL`.

---

## 5. Проверка после деплоя

- Открыть `https://<ваш-сервис>.onrender.com/` — страница входа; зарегистрироваться и сыграть партию.
- Проверить REST (замените хост):
  ```
  GET  https://<ваш-сервис>.onrender.com/api/score/floodfill
  POST https://<ваш-сервис>.onrender.com/api/score   (JSON как в gamestudio.http)
  ```
- Логи сборки/старта: Render Dashboard → ваш сервис → **Logs**.
  Успешный старт заканчивается строкой `Started GameStudioServer …`.

Обновление деплоя — просто `git push` (auto-deploy включён по умолчанию).

---

## 6. Ограничения бесплатного тарифа и как с ними жить

| Ограничение | Значение | Как жить |
|---|---|---|
| Render: засыпание | 15 мин без трафика → пробуждение ~1 мин (загрузочная страница) | Нормально для демо; «будить» можно открытием страницы |
| Render: ресурсы | 750 инстанс-часов/мес (хватает на 24/7 одного сервиса), 0.1 CPU, 512 MB | В Dockerfile heap ограничен `-XX:MaxRAMPercentage=75` |
| Neon: CU-часы | 100 CU-ч/мес; scale-to-zero после 5 мин без запросов | Пул уже настроен (`maximumPoolSize=2`, `minimumIdle=0`) — простаивающие соединения закрываются, БД засыпает и не тратит лимит; первый запрос после сна медленнее на ~0.5–1 с |
| Render free Postgres | удаляется через 30 дней | Поэтому БД — на Neon, не на Render |
| Сессии игры | в памяти инстанса | Текущая партия сбрасывается после засыпания/рестарта; очки/комменты в Neon — нет |

---

## 7. Запасные варианты (если Render/Neon не подойдут)

1. **Supabase (бесплатный Postgres) вместо Neon** — 500 МБ, но проект «замирает» после
   7 дней неактивности (оживает при входе через дашборд). Строка подключения конвертируется так же.
2. **Azure App Service (F1, бесплатно) + Azure Database for PostgreSQL Flexible Server
   (free 12 мес)** — Java запускается нативно без Docker: загрузить собранный jar
   (деплой JAR), строку БД в app settings. Минусы: F1 — 60 CPU-минут в день, бесплатная БД — только первый год.
3. **Google Cloud Run + Neon** — контейнер (наш `Dockerfile` подходит), 2 млн запросов/мес бесплатно,
   но для аккаунта нужна банковская карта.

---

## 8. Локальная проверка (опционально)

Локально Docker не установлен, поэтому проверить образ можно на другой машине:

```bash
docker build -t floodfill .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod \
  -e JDBC_DATABASE_URL="jdbc:postgresql://<neon-host>/neondb?user=...&password=...&sslmode=require" floodfill
```

Или собрать и запустить классически (нужны JDK 18+ и Maven):

```bash
mvn package
java -jar target/gamestudio-0.0.1-SNAPSHOT.jar   # веб-сервер на :8080
```
