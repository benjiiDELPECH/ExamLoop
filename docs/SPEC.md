# SPEC ExamLoop V1 — Prod-Ready

## Vision

Application mobile de révision intelligente combinant :
- **Adaptive Difficulty** : ajuste le niveau selon les performances
- **Spaced Repetition** : optimise la rétention avec répétition espacée
- **Distribution Strategy** : équilibre découverte et révision

Objectif : permettre à un utilisateur de maîtriser un sujet (ex: Spring) via des sessions quotidiennes optimisées.

---

## 1) Invariants non négociables

1. L'utilisateur peut utiliser l'app dès le premier lancement (Device ID, pas de signup)
2. Toutes les données sont liées à un `userId` (Device ID ou Supabase JWT)
3. L'API est la source de vérité (premium, quotas, items, scheduling)
4. Le mobile ne contient pas de logique d'apprentissage "décisionnelle"
5. L'algorithme hybride est testable unitairement (domain pur)

---

## 2) Domain Model

### Entités principales

```
Profile
├── userId (UUID)
├── premium (Boolean)
├── createdAt (Timestamp)

Goal (Exam)
├── id (UUID)
├── userId (UUID)
├── title (String)
├── description (String?)
├── isPublic (Boolean)  // Exam public partageable
├── createdAt (Timestamp)

Question
├── id (UUID)
├── goalId (UUID)
├── type (SINGLE_CHOICE | MULTIPLE_CHOICE | OPEN)
├── prompt (String)
├── choices (List<Choice>?)  // Pour MCQ
├── answer (String)
├── explanation (String?)
├── difficulty (EASY | MEDIUM | HARD | VERY_HARD)
├── chapter (String?)
├── tags (List<String>?)
├── createdAt (Timestamp)

Choice
├── id (UUID)
├── label (String)
├── isCorrect (Boolean)

QuestionAttempt
├── id (UUID)
├── questionId (UUID)
├── userId (UUID)
├── isCorrect (Boolean)
├── selectedChoiceIds (List<UUID>?)
├── timestamp (Timestamp)

ReviewState (Spaced Repetition)
├── questionId (UUID)
├── userId (UUID)
├── masteryLevel (NOVICE | LEARNING | COMPETENT | MASTERED)
├── successCount (Int)
├── failCount (Int)
├── lastReviewedAt (Timestamp?)
├── nextReviewAt (Timestamp)
├── optimalInterval (Int)  // Jours

UsageDaily
├── userId (UUID)
├── date (Date)
├── reviewsCount (Int)

TrainingSession
├── id (UUID)
├── userId (UUID)
├── goalId (UUID?)
├── status (NOT_STARTED | IN_PROGRESS | COMPLETED)
├── config (SessionConfig)
├── startedAt (Timestamp?)
├── completedAt (Timestamp?)
```

---

## 3) Algorithme Hybride

### 3.1 Adaptive Difficulty

```kotlin
// Analyse les 10 dernières tentatives
// Promotion si >75% succès
// Demotion si <40% succès
// Minimum 3 tentatives avant adaptation

enum class QuestionDifficulty {
    EASY,    // Niveau 1
    MEDIUM,  // Niveau 2
    HARD,    // Niveau 3
    VERY_HARD // Niveau 4
}
```

### 3.2 Spaced Repetition

```kotlin
// Intervalles Ebbinghaus (jours)
val SPACED_INTERVALS = listOf(1, 3, 7, 14, 30, 90)

// Mastery Levels
enum class MasteryLevel {
    NOVICE,    // < 2 tentatives
    LEARNING,  // < 3 tentatives OU < 70% succès
    COMPETENT, // >= 3 tentatives ET < 85% succès
    MASTERED   // >= 3 tentatives ET >= 85% succès
}

// Probabilité d'oubli
forgettingProbability = min(1.0, daysSince / (optimalInterval * 2.0))
```

### 3.3 Distribution Strategy

```kotlin
enum class DistributionStrategy(
    val reviewRatio: Double,
    val discoveryRatio: Double
) {
    EXPLORATION_FOCUSED(0.20, 0.80),  // Débutants
    DISCOVERY_PREFERRED(0.30, 0.70),  // Compétents
    BALANCED(0.50, 0.50),             // Expérimentés
    REVIEW_PREFERRED(0.70, 0.30),     // En difficulté
    MASTERY_FOCUSED(0.80, 0.20)       // Consolidation
}

// Détermination automatique
fun determineOptimal(totalAnswered: Int, avgSuccessRate: Double) = when {
    totalAnswered < 10 -> EXPLORATION_FOCUSED
    totalAnswered < 50 && avgSuccessRate > 0.7 -> DISCOVERY_PREFERRED
    avgSuccessRate < 0.5 -> REVIEW_PREFERRED
    else -> BALANCED
}
```

---

## 4) Quota & Premium

```kotlin
const val FREE_DAILY_REVIEW_LIMIT = 20

// Free: 20 reviews/jour
// Premium: illimité

// HTTP 429 si quota dépassé avec:
// errorCode: "QUOTA_EXCEEDED"
// meta: { quotaLimit, quotaUsed }
```

---

## 5) API Endpoints

### Bootstrap
```
POST /api/v1/bootstrap
→ Upsert Profile
→ Upsert UsageDaily
→ Return { profile, usage, dashboard }
```

### Goals (Exams)
```
GET  /api/v1/goals              → Liste goals (perso + publics)
POST /api/v1/goals              → Créer goal
GET  /api/v1/goals/{id}         → Détail goal
GET  /api/v1/goals/public       → Exams publics disponibles
```

### Questions
```
GET  /api/v1/questions?goalId=  → Liste questions d'un goal
POST /api/v1/questions          → Créer question
```

### Session
```
POST /api/v1/sessions           → Créer session (avec config)
GET  /api/v1/sessions/{id}/next → Prochaine question (algo hybride)
POST /api/v1/sessions/{id}/answer → Soumettre réponse
GET  /api/v1/sessions/{id}      → État session
```

### Review (legacy Leitner simple)
```
POST /api/v1/reviews            → Soumettre review { questionId, correct }
```

### Billing
```
POST /api/v1/billing/checkout   → URL Stripe Checkout
POST /api/v1/billing/webhook    → Webhook Stripe
```

---

## 6) Exams Publics

### Concept
- Certains exams sont **publics** et partagés entre utilisateurs
- Ex: "Spring Security Certification Prep" avec 50 questions
- L'utilisateur peut les ajouter à ses goals et tracker sa progression

### Premier Exam Public : Spring (MVP)
- ~20 questions variées
- Mix de difficultés (EASY, MEDIUM, HARD)
- Couvre : Core, Security, Data, Boot

---

## 7) Configuration Utilisateur (Future)

L'utilisateur pourra choisir :
- **Mode automatique** : l'algorithme décide
- **Mode révision** : force REVIEW_PREFERRED
- **Mode découverte** : force EXPLORATION_FOCUSED

---

## 8) Ordre d'implémentation

### Phase 1 : Fondations
- [ ] Backend Kotlin + architecture en couches
- [ ] Flyway schema V2 (difficulty, mastery, attempts)
- [ ] Domain models immutables
- [ ] Problem Details pour erreurs

### Phase 2 : Algorithme
- [ ] AdaptiveDifficultyService (domain pur)
- [ ] SpacedRepetitionService (domain pur)
- [ ] HybridLearningService (orchestration)
- [ ] Tests unitaires algorithme

### Phase 3 : API
- [ ] Sessions avec algo hybride
- [ ] Quota enforcement
- [ ] Bootstrap endpoint

### Phase 4 : Contenu
- [ ] 20 questions Spring (exam public)
- [ ] Migration Flyway pour seed

### Phase 5 : Billing
- [ ] Stripe Checkout
- [ ] Webhook idempotent
- [ ] Premium flag

### Phase 6 : Mobile
- [ ] Intégration nouvel algo
- [ ] Stats/Dashboard amélioré
- [ ] Paywall UX
