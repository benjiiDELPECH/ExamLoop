# DECISIONS.md — Architecture Decision Records

## ADR-001: Algorithme Hybride (Adaptive + Spaced)

**Date**: 2026-01-28

**Contexte**: 
Le projet exam-drilling utilisait un algorithme hybride combinant Adaptive Difficulty et Spaced Repetition, qui a prouvé son efficacité (84% de réussite sur Spring).

**Décision**:
Adopter l'algorithme hybride pour ExamLoop avec :
- AdaptiveDifficultyService : ajuste le niveau selon performances récentes
- SpacedRepetitionService : optimise la rétention avec répétition espacée
- DistributionStrategy : équilibre automatique découverte/révision

**Conséquences**:
- Plus complexe que Leitner simple
- Meilleurs résultats d'apprentissage prouvés
- Nécessite plus de données utilisateur (attempts)

---

## ADR-002: Kotlin au lieu de Java

**Date**: 2026-01-28

**Contexte**:
Le backend ExamLoop actuel est en Java. Le toolkit recommande Kotlin.

**Décision**:
Migrer vers Kotlin pour :
- Sealed classes pour résultats typés
- Null safety
- Data classes immutables
- Extensions functions
- Coroutines (future)

**Conséquences**:
- Réécriture du backend nécessaire
- Meilleure expressivité du code métier
- Alignement avec les guidelines toolkit

---

## ADR-003: Device ID pour auth initiale

**Date**: 2026-01-28

**Contexte**:
La SPEC originale mentionnait Supabase anonymous auth. L'implémentation actuelle utilise Device ID.

**Décision**:
Garder Device ID pour le MVP :
- Plus simple à implémenter
- Pas de dépendance Supabase
- Suffisant pour le flow anonymous → valeur → paywall

**Migration future possible** vers Supabase si besoin de :
- Email capture
- Cross-device sync
- Social login

**Conséquences**:
- Données liées au device, pas à l'utilisateur
- Perte de données si change de device
- Migration possible plus tard

---

## ADR-004: Exams Publics

**Date**: 2026-01-28

**Contexte**:
Besoin de contenu initial pour tester l'app et démontrer la valeur.

**Décision**:
Créer des "Exams Publics" :
- Créés par nous, partagés avec tous les utilisateurs
- Premier exam : Spring (~20 questions)
- Utilisateurs peuvent les ajouter à leurs goals
- Progression trackée individuellement

**Conséquences**:
- Valeur immédiate dès le premier lancement
- Moins de friction (pas besoin de créer ses questions)
- Possibilité de monétiser des packs premium plus tard

---

## ADR-005: Quota 20 reviews/jour

**Date**: 2026-01-28

**Contexte**:
Besoin d'un modèle freemium simple.

**Décision**:
- FREE_DAILY_REVIEW_LIMIT = 20
- Reset à minuit
- Premium = illimité

**Justification**:
- 20 reviews ≈ 10-15 minutes/jour
- Suffisant pour goûter la valeur
- Motivation à upgrader si usage intensif

**Conséquences**:
- Tracking de UsageDaily nécessaire
- Paywall après 20 reviews
- UX claire sur le quota restant
