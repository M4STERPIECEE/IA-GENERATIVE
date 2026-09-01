# Assistant IA RAG & MCP

Ce projet implémente un assistant IA capable de répondre à des questions en utilisant un corpus documentaire interne (RAG) et des outils externes dynamiques via un serveur MCP

## Architecture Globale

Le projet est divisé en deux modules Spring Boot distincts qui communiquent via HTTP :

1. **mcp-server (Port 8081)** : Un serveur indépendant implémentant le Model Context Protocol (MCP). Il expose 3 outils différents :
   - Outil météo (connexion à l'API publique Open-Meteo)
   - Outil de calcul de prêt immobilier (calcul mathématique)
   - Outil d'annuaire RH (lecture d'un fichier CSV local)
     Il contient aussi un filtre (OutputSanitizer) pour nettoyer les données sortantes

2. **agent (Port 8080)** : L'application principale qui expose l'API REST `/api/v1/chat`.
   - **Routage** : Un premier appel LLM (température 0) analyse la question et décide de la route à prendre (RAG, MCP, HYBRID, ou OUT_OF_SCOPE)
   - **RAG** : Utilise un `SimpleVectorStore` en mémoire pour indexer des fichiers Markdown au démarrage
   - **Orchestration** : sur la route, l'agent appelle le VectorStore, le serveur MCP (via un client HTTP Streamable), ou les deux en même temps pour générer la réponse finale
   - **Sécurité** : Un `PromptInjectionGuard` protège le LLM contre les instructions malveillantes qui pourraient se trouver dans les documents

## Prérequis et Lancement

- Java 21 minimum
- Une clé API pour un LLM (configuré pour utiliser Gemini via l'interface de compatibilité OpenAI)

### Configuration

Copiez le fichier `.env.example` vers `.env` à la racine du projet et ajoutez votre clé API :

```properties
AI_API_KEY=votre_cle_api_ici
AI_BASE_URL=https://generativelanguage.googleapis.com/v1beta/openai/
AI_CHAT_MODEL=gemini-3.1-pro
AI_EMBEDDING_MODEL=text-embedding-004
```

### Démarrage

Il faut d'abord lancer le serveur MCP, puis l'agent. Ouvrez deux terminaux séparés à la racine du projet :

**Terminal 1 (Serveur MCP)** :

```bash
./gradlew :mcp-server:bootRun
```

**Terminal 2 (Agent IA)** :
_(Assurez-vous que les variables d'environnement du fichier .env sont bien chargées dans ce terminal)_

```bash
./gradlew :agent:bootRun
```

### Tests (Smoke Test)

Vous pouvez tester l'API directement avec cURL ou Postman :

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "Quels sont les tarifs de iAgen Analyse Pro ?"}'
```

Le script d'évaluation automatique peut aussi être lancé pour tester tous les scénarios :

```bash
./gradlew :agent:bootRun --args='--spring.profiles.active=eval'
```

Le rapport sera généré dans `eval/report.md`.

## Choix tech

- **Java 21 & Spring Boot 4.1.1** : Standard robuste pour le backend, avec les threads virtuels activés si besoin
- **Spring AI 2.0.1** : Choisi pour son intégration native et standardisée de MCP, du RAG et des différents providers LLM
- **Lombok** : Pour réduire le boilerplate (constructeurs, loggers)
- **Routage explicite (2 appels LLM)** : J'ai préféré séparer la décision de routage de la génération de réponse. Cela coûte un peu plus cher en requêtes, mais ça garantit que le LLM ne se perd pas et évite les hallucinations de routage.
- **Architecture multi-modules Gradle** : Permet de bien isoler les responsabilités. Le serveur MCP pourrait être déployé sur une machine totalement différente dans un vrai contexte de production.

## Objectifs

**Socle obligatoire :**

- RAG fonctionnel avec ingestion, chunking (TokenTextSplitter) et indexation au démarrage.
- Serveur MCP fonctionnel et connecté à l'agent.
- Routage intelligent et traçable (RAG vs MCP vs Hybride).

**Pour aller plus loin (Bonus réalisés) :**

- Gestion des erreurs et fallbacks : Si le serveur MCP crash ou timeout, l'orchestrateur log l'erreur et tente de répondre avec le RAG en fallback au lieu de faire planter la requête.
- 3 outils implémentés sur le serveur MCP couvrant 3 domaines distincts (API Web, Algorithme local, Lecture de fichier).
- Script d'évaluation automatique (`EvalRunner`) qui teste 10 questions différentes et valide le comportement du routage et le contenu de la réponse.
- Protection contre l'injection de prompt (filtrage par regex et encapsulation `<untrusted-data>`). Le fichier `docs/piege_injection.md` sert à tester cette protection.

## Limites et améliorations possibles

- Le `SimpleVectorStore` en mémoire n'est pas viable pour un gros volume en production. Il faudrait le remplacer par une base vectorielle comme PgVector ou Qdrant.
- Les embeddings sont calculés via l'API réseau, ce qui ralentit l'ingestion au démarrage. Un modèle local (type Ollama) pour les embeddings serait plus rapide et moins coûteux.
- Il manque un système de cache (ex: Redis) pour éviter d'appeler le LLM si la même question est posée deux fois.
- L'API ne gère pas le streaming (SSE) pour le moment. L'ajout du streaming améliorerait considérablement l'expérience utilisateur finale (UX).

## Structure du projet

```text
IA/
├── build.gradle                 # Config multi-modules
├── settings.gradle              # rootProject + include 'mcp-server', 'agent'
├── .env.example                 # Template pour les variables d'environnement (clés API)
├── docs/                        # Corpus RAG (Documents markdown)
│   ├── manuel_rh.md
│   ├── catalogue_produits.md
│   ├── politique_securite.md
│   └── piege_injection.md       # Fichier pour tester la protection anti-injection
├── eval/                        # Fichiers liés au script d'évaluation automatique
│   ├── questions.json
│   └── report.md
├── mcp-server/                  # MODULE 1 : Serveur MCP (port 8081)
│   └── src/main/java/com/iagen/mcp/
│       ├── McpServerApplication.java
│       ├── config/McpToolsConfig.java
│       ├── tools/WeatherTool.java
│       ├── tools/LoanCalculatorTool.java
│       ├── tools/EmployeeDirectoryTool.java
│       └── security/OutputSanitizer.java
└── agent/                       # MODULE 2 : Agent IA (port 8080)
    └── src/main/java/com/iagen/agent/
        ├── AgentApplication.java
        ├── config/AiConfig.java
        ├── rag/IngestionService.java
        ├── rag/RagService.java
        ├── routing/RoutingDecision.java
        ├── routing/RouterService.java
        ├── orchestration/TraceCollector.java
        ├── orchestration/OrchestratorService.java
        ├── security/PromptInjectionGuard.java
        ├── web/ChatController.java
        ├── web/dto/ChatRequest.java
        ├── web/dto/ChatResponse.java
        └── eval/EvalRunner.java
```
