# Cursor Rules — ExamLoop

Ces règles guident Cursor AI pour produire du code cohérent et prod-ready.

## Règles actives

| Fichier | Description |
|---------|-------------|
| `00_DOCTRINE.mdc` | Principes non négociables (scope, discipline) |
| `10_ARCHITECTURE.mdc` | Structure en 4 couches (domain/application/infra/api) |
| `20_LEARNING_ALGORITHM.mdc` | Algorithme hybride (Adaptive + Spaced) |
| `30_DEBUG_PROTOCOL.mdc` | Protocole de debug (2 tentatives max) |
| `40_DATABASE.mdc` | Postgres + Flyway |
| `50_MONETIZATION.mdc` | Funnel anonymous → paywall |
| `60_TESTING.mdc` | Tests comportementaux |

## Principes clés

1. **Scope minimal** — Ne modifier que ce qui est nécessaire
2. **Architecture en couches** — domain pur, sans Spring
3. **Algorithme hybride** — Adaptive Difficulty + Spaced Repetition
4. **Kotlin idiomatique** — sealed class, data class, immutabilité
5. **Tests sur l'algorithme** — Invariants protégés

## Source

Basé sur le [delpech-toolskit](../../../delpech-toolskit/), adapté pour ExamLoop.
