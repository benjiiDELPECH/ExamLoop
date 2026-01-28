# ExamLoop API Documentation

Base URL: `http://localhost:8080` (dev) ou `https://api.examloop.com` (prod)

## Authentication

Toutes les requêtes (sauf webhook) doivent inclure:
```
X-Device-Id: <unique-device-id>
Content-Type: application/json
```

---

## Endpoints

### 1. Bootstrap

Initialise l'app au démarrage.

```http
POST /api/v1/bootstrap
```

**Response 200:**
```json
{
  "profile": {
    "userId": "device-123",
    "premium": false
  },
  "usage": {
    "date": "2026-01-28",
    "reviewsUsed": 3,
    "reviewsLimit": 20,
    "remaining": 17,
    "percentUsed": 15,
    "premium": false
  },
  "dashboard": {
    "dueCount": 12,
    "totalQuestions": 50,
    "goalsCount": 3,
    "masteredCount": 8
  }
}
```

---

### 2. Goals (Exams)

#### List Goals
```http
GET /api/v1/goals
```

**Response 200:**
```json
{
  "goals": [
    {
      "id": 1,
      "title": "Spring Certification Prep",
      "description": "...",
      "isPublic": true,
      "stats": {
        "items": 20,
        "due": 5
      }
    }
  ]
}
```

#### Create Goal
```http
POST /api/v1/goals
```

**Request:**
```json
{
  "title": "Kubernetes",
  "description": "Exam CKA prep",
  "isPublic": false
}
```

**Response 201:**
```json
{
  "id": 2,
  "title": "Kubernetes",
  "description": "Exam CKA prep",
  "isPublic": false,
  "stats": { "items": 0, "due": 0 }
}
```

---

### 3. Sessions

Génère une session d'apprentissage avec l'algorithme hybride.

```http
POST /api/v1/sessions
```

**Request:**
```json
{
  "goalId": 1,
  "limit": 10,
  "difficulty": "MEDIUM",
  "strategy": "BALANCED"
}
```

**Response 200:**
```json
{
  "sessionId": "session_abc123",
  "questions": [
    {
      "id": 1,
      "goalId": 1,
      "type": "SINGLE_CHOICE",
      "prompt": "Quelle annotation...",
      "choices": [
        { "id": "uuid-1", "label": "@Component" },
        { "id": "uuid-2", "label": "@Bean" }
      ],
      "difficulty": "EASY",
      "chapter": "Spring Core"
    }
  ],
  "strategy": "BALANCED",
  "reviewCount": 6,
  "discoveryCount": 4,
  "limits": { "maxItems": 10 }
}
```

---

### 4. Reviews

Soumet une réponse à une question.

```http
POST /api/v1/reviews
```

**Request:**
```json
{
  "questionId": 1,
  "correct": true,
  "selectedChoiceIds": ["uuid-1"]
}
```

**Response 200:**
```json
{
  "questionId": 1,
  "result": "CORRECT",
  "reviewState": {
    "masteryLevel": "COMPETENT",
    "successCount": 5,
    "failCount": 1,
    "nextReviewAt": "2026-02-03T10:00:00Z"
  },
  "usage": {
    "date": "2026-01-28",
    "reviewsUsed": 4,
    "reviewsLimit": 20,
    "remaining": 16,
    "percentUsed": 20,
    "premium": false
  }
}
```

**Response 429 (Quota):**
```json
{
  "type": "about:blank",
  "title": "Quota exceeded",
  "status": 429,
  "detail": "Quota journalier dépassé. Passez à Premium pour continuer.",
  "errorCode": "QUOTA_EXCEEDED",
  "meta": {
    "quotaLimit": 20,
    "quotaUsed": 20
  }
}
```

---

### 5. AI Generation (Premium)

Génère un examen complet via IA.

```http
POST /api/v1/exams/generate
```

**Request:**
```json
{
  "topic": "Spring Security",
  "description": "Focus sur OAuth2 et JWT",
  "questionCount": 15,
  "difficultyMix": "BALANCED",
  "questionTypes": ["SINGLE_CHOICE", "OPEN"],
  "language": "fr"
}
```

**Response 201:**
```json
{
  "examId": 5,
  "title": "Spring Security",
  "description": "Focus sur OAuth2 et JWT",
  "questionsGenerated": 15,
  "questions": [...],
  "topicAnalysis": {
    "mainTopic": "Spring Security",
    "subtopics": ["OAuth2", "JWT", "CSRF", "CORS"],
    "suggestedChapters": [...],
    "depth": "INTERMEDIATE"
  },
  "metadata": {
    "model": "gpt-4o-mini",
    "tokensUsed": 4521,
    "generationTimeMs": 8432
  }
}
```

**Response 402 (Not Premium):**
```json
{
  "type": "about:blank",
  "title": "Premium required",
  "status": 402,
  "detail": "La génération IA nécessite un abonnement Premium",
  "errorCode": "PREMIUM_REQUIRED",
  "meta": {
    "upgradeUrl": "/api/v1/billing/checkout"
  }
}
```

---

### 6. Billing

#### Create Checkout Session
```http
POST /api/v1/billing/checkout
```

**Response 200:**
```json
{
  "sessionId": "cs_xxx",
  "url": "https://checkout.stripe.com/..."
}
```

#### Get Billing Status
```http
GET /api/v1/billing/status
```

**Response 200:**
```json
{
  "premium": true,
  "features": [
    "unlimited_reviews",
    "ai_question_generation",
    "priority_support",
    "export_data"
  ]
}
```

---

## Error Codes

| Code | HTTP | Description |
|------|------|-------------|
| `VALIDATION_FAILED` | 400 | Validation error |
| `FORBIDDEN` | 403 | Access denied |
| `NOT_FOUND` | 404 | Resource not found |
| `GOAL_NOT_FOUND` | 404 | Goal not found |
| `QUESTION_NOT_FOUND` | 404 | Question not found |
| `QUOTA_EXCEEDED` | 429 | Daily quota reached |
| `PREMIUM_REQUIRED` | 402 | Feature requires premium |
| `GENERATION_FAILED` | 503 | AI generation failed |

---

## Enums

### QuestionDifficulty
- `EASY`
- `MEDIUM`
- `HARD`
- `VERY_HARD`

### QuestionType
- `SINGLE_CHOICE`
- `MULTIPLE_CHOICE`
- `OPEN`

### MasteryLevel
- `NOVICE`
- `LEARNING`
- `COMPETENT`
- `MASTERED`

### DistributionStrategy
- `EXPLORATION_FOCUSED` (80% new)
- `DISCOVERY_PREFERRED` (70% new)
- `BALANCED` (50/50)
- `REVIEW_PREFERRED` (70% review)
- `MASTERY_FOCUSED` (80% review)

### DifficultyMix
- `EASY_FOCUSED`
- `BALANCED`
- `HARD_FOCUSED`
- `PROGRESSIVE`
