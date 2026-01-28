# ExamLoop

A spaced repetition learning application built with Spring Boot and Expo. ExamLoop helps you master any subject using the proven Leitner system for optimal memory retention.

## Architecture

This is a monorepo containing:

- **services/api**: Spring Boot 3 REST API with PostgreSQL and Flyway migrations
- **apps/mobile**: Expo (React Native) TypeScript mobile application
- **docker-compose.yml**: Orchestration for PostgreSQL and API services

## Features

### API Service
- **Authentication**: Anonymous login with device ID tracking
- **Goals CRUD**: Create and manage learning goals
- **Items CRUD**: Create flashcards with questions and answers
- **Leitner System**: Implements boxes 1-5 for spaced repetition
- **Session Management**: Get today's items for review
- **Review System**: Mark items as correct/wrong to move through boxes
- **Billing**: Mock checkout and webhook endpoints
- **Actuator**: Health checks and metrics enabled

### Mobile App
- **Board Screen**: View all goals and navigate to items
- **AddItem Screen**: Create and view flashcards for a goal
- **Session Screen**: Review today's due items with the Leitner algorithm
- **Paywall Screen**: Premium subscription flow (opens checkout URL)
- **Device ID**: Automatically generates and persists device identifier
- **API Integration**: Full REST API client with automatic device ID injection

## Prerequisites

- **Docker & Docker Compose** (for running API + PostgreSQL)
- **Node.js 18+** (for mobile app development)
- **npm or yarn** (package manager)
- Java 17+ and Maven (optional, only if running API without Docker)

## Quick Start

### 1. Start the Backend (API + Database)

First, build the API JAR:

```bash
# From the root directory
cd services/api
mvn clean package -DskipTests
cd ../..
```

Then start the services:

```bash
docker compose up --build
```

Alternatively, use the start script:

```bash
./start.sh
```

This will:
- Start PostgreSQL on `localhost:5432`
- Build and start the API on `localhost:8080`
- Run Flyway migrations automatically
- Enable health checks at `http://localhost:8080/actuator/health`

Wait for the logs to show:
```
examloop-api    | Started ExamLoopApplication in X.XX seconds
```

### 2. Start the Mobile App

```bash
# Navigate to the mobile app directory
cd apps/mobile

# Install dependencies (first time only)
npm install

# Start Expo dev server
npm start
```

Then:
- Press `w` to open in web browser
- Press `a` to open in Android emulator
- Press `i` to open in iOS simulator (macOS only)
- Scan QR code with Expo Go app on your phone

### 3. Configure API URL (if needed)

By default, the mobile app connects to `http://localhost:8080`. If running on a physical device or different network:

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
