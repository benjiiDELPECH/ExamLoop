# ExamLoop

Application mobile de révision intelligente combinant **Adaptive Difficulty** et **Spaced Repetition** pour un apprentissage optimal.

> 🎯 Testé et prouvé : 84% de réussite sur la certification Spring !

## Vision

ExamLoop utilise un **algorithme hybride** qui :
- **Adapte la difficulté** selon tes performances (monte/descend de niveau)
- **Optimise la rétention** avec répétition espacée (courbe d'Ebbinghaus)
- **Équilibre automatiquement** découverte et révision

## Architecture

Monorepo avec :

- **services/api**: Spring Boot 3 + Kotlin (migration en cours) + PostgreSQL + Flyway
- **apps/mobile**: Expo (React Native) TypeScript
- **docs/**: Spécifications et décisions d'architecture
- **.cursor/rules/**: Règles Cursor pour code cohérent

## Features

### Algorithme Hybride
- **Adaptive Difficulty**: 4 niveaux (EASY → VERY_HARD), ajustement automatique
- **Spaced Repetition**: Intervalles optimisés (1, 3, 7, 14, 30, 90 jours)
- **Distribution Strategy**: 5 modes (exploration → consolidation)

### API Service
- **Authentication**: Device ID (anonymous first)
- **Goals/Exams**: Exams personnels + exams publics partagés
- **Questions**: MCQ, Single Choice, Open questions
- **Sessions**: Génération intelligente via algo hybride
- **Quota**: 20 reviews/jour gratuit, Premium illimité
- **Billing**: Stripe Checkout + webhooks

### Mobile App
- **Board**: Vue d'ensemble des exams et progression
- **Session**: Révision avec feedback immédiat
- **Stats**: Mastery level, streak, questions à réviser
- **Paywall**: Flow premium intégré

## Documentation

- **[docs/SPEC.md](docs/SPEC.md)** — Spécification complète V1
- **[docs/DECISIONS.md](docs/DECISIONS.md)** — Décisions d'architecture (ADRs)
- **[docs/LATER.md](docs/LATER.md)** — Améliorations futures (pas maintenant)
- **[.cursor/rules/](/.cursor/rules/)** — Règles Cursor pour code cohérent

## Prerequisites

- **Docker & Docker Compose** (API + PostgreSQL)
- **Node.js 18+** (mobile app)
- **Java 17+ / Kotlin** (backend dev)

## Quick Start

### 1. Start Backend

```bash
# Build and start
docker compose up --build

# Health check
curl http://localhost:8080/actuator/health
```

### 2. Start Mobile

```bash
cd apps/mobile
npm install
npm start
```

### 3. Configure API URL (physical device)

Edit `apps/mobile/src/api/client.ts`:
```typescript
const API_BASE_URL = 'http://YOUR_IP_ADDRESS:8080';
```

## API Endpoints

### Authentication
- `POST /anon/login` - Login with device ID (header: `X-Device-Id` or body)

### Goals
- `GET /goals` - List all goals
- `POST /goals` - Create a goal
- `GET /goals/{id}` - Get a goal
- `PUT /goals/{id}` - Update a goal
- `DELETE /goals/{id}` - Delete a goal

### Items
- `GET /items` - List all items
- `POST /items` - Create an item
- `GET /items/{id}` - Get an item
- `PUT /items/{id}` - Update an item
- `DELETE /items/{id}` - Delete an item

### Session
- `GET /session/today` - Get items due for review today

### Review
- `POST /review/{id}` - Submit review (body: `{"correct": true/false}`)

### Billing
- `POST /billing/checkout` - Create checkout session
- `POST /billing/webhook` - Handle payment webhooks

### Actuator
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Metrics

All endpoints (except `/anon/login`) require the `X-Device-Id` header.

## Development

### Running API Locally (without Docker)

```bash
cd services/api

# Make sure PostgreSQL is running
# Update application.properties with your database credentials

mvn spring-boot:run
```

### Running Tests

```bash
cd services/api
mvn test
```

### Database Migrations

Flyway migrations are in `services/api/src/main/resources/db/migration/`

To add a new migration:
1. Create a new file: `V2__description.sql`
2. Restart the API (migrations run automatically on startup)

## Leitner Box System

The app uses a 5-box Leitner system for spaced repetition:

- **Box 1**: Review in 1 day
- **Box 2**: Review in 3 days
- **Box 3**: Review in 7 days
- **Box 4**: Review in 14 days
- **Box 5**: Review in 30 days

When you mark an item as:
- **Correct**: Moves to the next box (max Box 5)
- **Wrong**: Returns to Box 1

## Troubleshooting

### API won't start
- Check if port 8080 is already in use: `lsof -i :8080`
- Check database connection in `services/api/src/main/resources/application.properties`
- View logs: `docker-compose logs api`

### Database connection errors
- Ensure PostgreSQL is running: `docker-compose ps`
- Check credentials match in docker-compose.yml and application.properties

### Mobile app can't connect to API
- Ensure API is running: `curl http://localhost:8080/actuator/health`
- If using physical device, update API_BASE_URL to your computer's IP address
- Check firewall settings

### Expo not starting
- Clear cache: `npm start -- --clear`
- Delete node_modules and reinstall: `rm -rf node_modules && npm install`

## Project Structure

```
ExamLoop/
├── services/
│   └── api/                      # Spring Boot API
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/examloop/api/
│       │   │   │   ├── controller/    # REST controllers
│       │   │   │   ├── service/       # Business logic
│       │   │   │   ├── repository/    # JPA repositories
│       │   │   │   ├── model/         # Entity models
│       │   │   │   └── dto/           # Data transfer objects
│       │   │   └── resources/
│       │   │       ├── application.properties
│       │   │       └── db/migration/  # Flyway migrations
│       │   └── test/
│       ├── pom.xml
│       └── Dockerfile
├── apps/
│   └── mobile/                   # Expo mobile app
│       ├── src/
│       │   ├── screens/          # UI screens
│       │   ├── api/              # API client
│       │   └── utils/            # Utilities (deviceId)
│       ├── App.tsx
│       └── package.json
├── docker-compose.yml
└── README.md
```

## License

MIT
