-- V3: Seed public Spring exam with 20 questions
-- Questions variées couvrant Spring Core, Boot, Security, Data

-- ============================================================================
-- CREATE PUBLIC SPRING EXAM GOAL
-- ============================================================================
INSERT INTO goals (device_id, title, description, is_public, created_at, updated_at)
VALUES (
    'SYSTEM',
    'Spring Certification Prep',
    'Préparation à la certification Spring Professional. 20 questions couvrant Core, Boot, Security et Data.',
    TRUE,
    NOW(),
    NOW()
);

-- Get the goal ID for inserting questions
-- Note: Using subquery to reference the goal we just created

-- ============================================================================
-- SPRING CORE QUESTIONS (5)
-- ============================================================================

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quelle annotation permet de marquer une classe comme un bean Spring géré par le conteneur IoC ?',
    '@Component',
    1, NOW(), 'EASY', 'Spring Core',
    '@Component est l''annotation de base pour déclarer un bean Spring. @Service, @Repository et @Controller sont des spécialisations de @Component avec une sémantique métier.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quelle est la différence entre @Autowired et @Inject ?',
    '@Autowired est spécifique à Spring, @Inject est standard Java (JSR-330). Les deux permettent l''injection de dépendances.',
    1, NOW(), 'MEDIUM', 'Spring Core',
    '@Autowired offre plus de fonctionnalités (required=false), mais @Inject est portable vers d''autres conteneurs DI.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quel est le scope par défaut d''un bean Spring ?',
    'Singleton',
    1, NOW(), 'EASY', 'Spring Core',
    'Par défaut, un bean Spring est un singleton. Une seule instance est créée et partagée dans tout le contexte. Autres scopes : prototype, request, session, application.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Qu''est-ce que l''Inversion of Control (IoC) ?',
    'Un principe où le framework contrôle le flux d''exécution et la création des objets, plutôt que le code applicatif.',
    1, NOW(), 'MEDIUM', 'Spring Core',
    'IoC est le principe fondamental de Spring. Le conteneur gère le cycle de vie des beans et leurs dépendances. L''injection de dépendances est une forme d''IoC.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quelle annotation utiliser pour qu''une méthode soit appelée après l''initialisation d''un bean ?',
    '@PostConstruct',
    1, NOW(), 'MEDIUM', 'Spring Core',
    '@PostConstruct (JSR-250) est appelée après l''injection des dépendances. Alternative : implémenter InitializingBean.afterPropertiesSet() ou utiliser init-method.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

-- ============================================================================
-- SPRING BOOT QUESTIONS (5)
-- ============================================================================

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quelle annotation active l''auto-configuration Spring Boot ?',
    '@EnableAutoConfiguration (incluse dans @SpringBootApplication)',
    1, NOW(), 'EASY', 'Spring Boot',
    '@SpringBootApplication combine @Configuration, @EnableAutoConfiguration et @ComponentScan. L''auto-configuration configure automatiquement les beans selon le classpath.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Comment exclure une auto-configuration spécifique en Spring Boot ?',
    '@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})',
    1, NOW(), 'HARD', 'Spring Boot',
    'On peut aussi utiliser spring.autoconfigure.exclude dans application.properties. Utile quand on veut configurer manuellement un composant.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quel est le rôle du fichier application.properties ?',
    'Configurer les propriétés de l''application Spring Boot (port, datasource, logging, etc.)',
    1, NOW(), 'EASY', 'Spring Boot',
    'Spring Boot charge automatiquement application.properties ou application.yml. On peut avoir des profils : application-dev.properties, application-prod.properties.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Qu''est-ce qu''un Spring Boot Starter ?',
    'Une dépendance qui regroupe toutes les libs nécessaires pour une fonctionnalité (ex: spring-boot-starter-web)',
    1, NOW(), 'MEDIUM', 'Spring Boot',
    'Les starters simplifient la gestion des dépendances. spring-boot-starter-web inclut Spring MVC, Tomcat embedded, Jackson, validation, etc.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Comment accéder à une propriété custom dans application.properties ?',
    '@Value("${ma.propriete}") ou @ConfigurationProperties',
    1, NOW(), 'MEDIUM', 'Spring Boot',
    '@Value pour des valeurs simples, @ConfigurationProperties pour mapper un groupe de propriétés vers un objet. @ConfigurationProperties est type-safe et recommandé.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

-- ============================================================================
-- SPRING SECURITY QUESTIONS (5)
-- ============================================================================

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quelle annotation active Spring Security dans une application Spring Boot ?',
    '@EnableWebSecurity',
    1, NOW(), 'EASY', 'Spring Security',
    '@EnableWebSecurity active la configuration de sécurité web. Depuis Spring Boot 2.7+, elle est optionnelle si on a spring-boot-starter-security dans le classpath.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Qu''est-ce que le SecurityContextHolder ?',
    'Un conteneur qui stocke les informations d''authentification de l''utilisateur courant (thread-local par défaut)',
    1, NOW(), 'HARD', 'Spring Security',
    'SecurityContextHolder utilise une stratégie ThreadLocal par défaut. Il contient un SecurityContext qui contient un Authentication avec le principal et les authorities.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quelle est la différence entre @PreAuthorize et @Secured ?',
    '@PreAuthorize supporte SpEL (expressions), @Secured accepte uniquement des noms de rôles',
    1, NOW(), 'HARD', 'Spring Security',
    '@PreAuthorize("hasRole(''ADMIN'') and #id == principal.id") est plus flexible. @Secured("ROLE_ADMIN") est plus simple. Les deux nécessitent @EnableMethodSecurity.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Comment configurer une authentification HTTP Basic en Spring Security ?',
    'httpSecurity.httpBasic(Customizer.withDefaults())',
    1, NOW(), 'MEDIUM', 'Spring Security',
    'HTTP Basic envoie username:password encodé en Base64 dans le header Authorization. Simple mais pas sécurisé sans HTTPS. Utilisé pour APIs internes ou tests.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Qu''est-ce qu''un PasswordEncoder et pourquoi est-il important ?',
    'Interface pour hasher les mots de passe. BCryptPasswordEncoder est recommandé pour sécuriser le stockage.',
    1, NOW(), 'MEDIUM', 'Spring Security',
    'Ne jamais stocker les mots de passe en clair ! BCrypt inclut un salt et un cost factor. DelegatingPasswordEncoder permet de supporter plusieurs encodeurs (migration).',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

-- ============================================================================
-- SPRING DATA JPA QUESTIONS (5)
-- ============================================================================

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quelle annotation marque une classe comme entité JPA ?',
    '@Entity',
    1, NOW(), 'EASY', 'Spring Data JPA',
    '@Entity (javax.persistence / jakarta.persistence) indique que la classe est mappée vers une table. Nécessite aussi @Id pour la clé primaire.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Comment créer une méthode de requête dérivée en Spring Data JPA ?',
    'Définir une méthode dans l''interface Repository avec un nom qui suit la convention (ex: findByEmailAndStatus)',
    1, NOW(), 'MEDIUM', 'Spring Data JPA',
    'Spring Data parse le nom de méthode et génère la requête. findByStatusOrderByCreatedAtDesc, existsByEmail, countByCategory sont des exemples valides.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Quelle est la différence entre FetchType.LAZY et FetchType.EAGER ?',
    'LAZY charge les données à la demande, EAGER charge immédiatement avec l''entité parente',
    1, NOW(), 'HARD', 'Spring Data JPA',
    'LAZY est recommandé pour les collections (@OneToMany, @ManyToMany). EAGER peut causer le problème N+1. Utilisez JOIN FETCH ou @EntityGraph pour contrôler le chargement.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Comment écrire une requête JPQL custom dans un Repository Spring Data ?',
    '@Query("SELECT u FROM User u WHERE u.email = :email")',
    1, NOW(), 'MEDIUM', 'Spring Data JPA',
    '@Query permet d''écrire du JPQL (orienté objet) ou du SQL natif avec nativeQuery=true. Les paramètres sont bindés avec :nom ou ?1.',
    'SINGLE_CHOICE',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

INSERT INTO items (goal_id, device_id, question, answer, box, next_review, difficulty, chapter, explanation, item_type, created_at, updated_at)
SELECT id, 'SYSTEM',
    'Qu''est-ce que le problème N+1 et comment l''éviter ?',
    'Problème où N requêtes additionnelles sont exécutées pour charger les relations. Solution : JOIN FETCH ou @EntityGraph.',
    1, NOW(), 'VERY_HARD', 'Spring Data JPA',
    'Exemple : charger 100 users avec leurs commandes = 1 requête users + 100 requêtes commandes. JOIN FETCH u.orders ou @EntityGraph(attributePaths = "orders") résout le problème.',
    'OPEN',
    NOW(), NOW()
FROM goals WHERE title = 'Spring Certification Prep' AND is_public = TRUE;

-- ============================================================================
-- Verify insertion
-- ============================================================================
-- SELECT COUNT(*) FROM items WHERE goal_id = (SELECT id FROM goals WHERE title = 'Spring Certification Prep');
