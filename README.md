# 📚 Library DevOps

[![CI](https://github.com/AissaMehenni/library-devops/actions/workflows/ci.yml/badge.svg)](https://github.com/AissaMehenni/library-devops/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

Application de gestion de bibliothèque construite en architecture **microservices**. Projet DevOps EFREI illustrant la mise en œuvre d'une chaîne complète : conteneurisation, base de données, frontend SPA, intégration continue et couverture de tests.

---

## 🏗️ Architecture

```
                         ┌───────────────────────┐
                         │   Frontend (React)    │
                         │   localhost:3000      │
                         └──────────┬────────────┘
                                    │ HTTP
              ┌─────────────────────┴─────────────────────┐
              │                                           │
   ┌──────────▼──────────┐                  ┌─────────────▼─────────┐
   │   book-service      │                  │   member-service      │
   │   Spring Boot       │                  │   Spring Boot         │
   │   localhost:8081    │                  │   localhost:8082      │
   └──────────┬──────────┘                  └─────────────┬─────────┘
              │ JDBC                                      │ JDBC
   ┌──────────▼──────────┐                  ┌─────────────▼─────────┐
   │   db-book           │                  │   db-member           │
   │   PostgreSQL 16     │                  │   PostgreSQL 16       │
   └─────────────────────┘                  └───────────────────────┘
```

Chaque microservice possède sa propre base PostgreSQL (pattern *database-per-service*). L'ensemble est orchestré par **Docker Compose** sur un réseau bridge dédié.

---

## 🛠️ Stack technique

| Couche       | Technologies                                                |
|--------------|-------------------------------------------------------------|
| Backend      | Java 21, Spring Boot 3.5.13, Spring Web, Spring Data JPA    |
| Base de données | PostgreSQL 16 (prod) · H2 in-memory (tests)              |
| Frontend     | React 18, Vite 5, TypeScript 5, React Router 6              |
| Build        | Gradle 8, npm                                               |
| Tests        | JUnit 5, Mockito, MockMvc, Jacoco (seuil ≥ 80 %)            |
| Conteneurs   | Docker (multi-stage), Docker Compose, Nginx                 |
| CI/CD        | GitHub Actions                                              |

---

## 📁 Structure du dépôt

```
library-devops/
├── book-service/              # Microservice livres (port 8081)
│   ├── src/main/java/com/library/book/
│   │   ├── data/              # Entité JPA + repository
│   │   ├── services/          # Logique métier (emprunt / retour)
│   │   └── controllers/       # Endpoints REST
│   └── Dockerfile             # Multi-stage gradle → JRE 21
├── member-service/            # Microservice membres (port 8082)
│   └── ... (même structure)
├── frontend/                  # SPA React + Vite
│   ├── src/
│   │   ├── api/               # Clients HTTP typés
│   │   └── pages/             # Pages Livres / Membres
│   └── Dockerfile             # Multi-stage node → nginx
├── .github/workflows/ci.yml   # Pipeline CI
├── docker-compose.yml         # Orchestration complète
└── README.md
```

---

## 🚀 Lancer le projet en local

**Prérequis :** Docker Desktop (ou Docker Engine + Compose v2).

```bash
git clone https://github.com/AissaMehenni/library-devops.git
cd library-devops
docker compose up --build
```

Le premier démarrage construit les images (Gradle + npm) — comptez quelques minutes. Les services attendent que leurs bases PostgreSQL soient *healthy* avant de démarrer.

Pour arrêter et nettoyer les volumes :

```bash
docker compose down -v
```

---

## 🌐 URLs d'accès

| Service        | URL                       | Description                         |
|----------------|---------------------------|-------------------------------------|
| Frontend       | http://localhost:3000     | Interface React (Livres / Membres)  |
| book-service   | http://localhost:8081/books   | API REST livres                 |
| member-service | http://localhost:8082/members | API REST membres                |

---

## 🧪 Lancer les tests

Les tests utilisent H2 en mémoire — aucune dépendance Docker requise.

```bash
# Tests du book-service (avec rapport Jacoco)
cd book-service
./gradlew test jacocoTestReport

# Tests du member-service
cd ../member-service
./gradlew test jacocoTestReport
```

Les rapports HTML sont générés dans `build/reports/tests/test/` (tests) et `build/reports/jacoco/test/html/` (couverture).

Pour vérifier le seuil de couverture (≥ 80 %) :

```bash
./gradlew jacocoTestCoverageVerification
```

---

## ⚙️ Pipeline CI

Le workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) s'exécute sur chaque `push` et `pull_request` :

1. **`backend-test`** *(matrix : book-service, member-service)*
   - Build Gradle, exécution des tests JUnit 5
   - Génération du rapport Jacoco et **vérification du seuil ≥ 80 %**
   - Upload des artefacts (rapports de tests + couverture)
2. **`frontend-build`**
   - Installation des dépendances npm
   - Type-checking TypeScript (`tsc -b`)
   - Build production Vite
3. **`docker-build`** *(matrix : book-service, member-service, frontend)*
   - Construction des images Docker via Buildx (sans push)

Les jobs Docker dépendent du succès des tests et du build frontend.

---

## ✨ Fonctionnalités

### book-service (port 8081)

| Méthode | Endpoint                | Description                          |
|---------|-------------------------|--------------------------------------|
| GET     | `/books`                | Liste tous les livres                |
| GET     | `/books/{id}`           | Détail d'un livre                    |
| POST    | `/books`                | Créer un livre                       |
| PUT     | `/books/{id}`           | Mettre à jour un livre               |
| DELETE  | `/books/{id}`           | Supprimer un livre                   |
| POST    | `/books/borrow/{id}`    | Emprunter un livre (409 si indispo.) |
| POST    | `/books/return/{id}`    | Rendre un livre                      |

### member-service (port 8082)

| Méthode | Endpoint           | Description                          |
|---------|--------------------|--------------------------------------|
| GET     | `/members`         | Liste tous les membres               |
| GET     | `/members/{id}`    | Détail d'un membre                   |
| POST    | `/members`         | Créer un membre (email unique)       |
| PUT     | `/members/{id}`    | Mettre à jour un membre              |
| DELETE  | `/members/{id}`    | Supprimer un membre                  |

### Frontend

- Page **Livres** : liste, ajout, emprunt / retour, suppression.
- Page **Membres** : liste, ajout (avec validation e-mail), suppression.
- Navigation client-side via React Router.

---

## 📝 Licence

Projet académique — EFREI Paris.
