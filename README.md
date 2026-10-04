# TrustDesk - AI-First Support Operations Platform

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-pgvector-336791)
![LangChain4j](https://img.shields.io/badge/LangChain4j-AI%20Orchestration-orange)
![React](https://img.shields.io/badge/React-18-blue)
![Vite](https://img.shields.io/badge/Vite-Build%20Tool-purple)
![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4.0-06B6D4)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED)

TrustDesk is an enterprise-grade, AI-first support operations platform. It integrates LLM orchestration with semantic search (RAG) to autonomously triage incoming support tickets, enforce security guardrails, and generate policy-grounded draft replies.

To ensure safety and compliance, the system requires deterministic human approval for executing any sensitive AI-proposed tool actions (e.g., issuing refunds or replacement orders).

## 🏛️ Architecture

This project uses a structured monorepo separating the Java backend from the React frontend, bridged by a REST API.

* **Backend (Spring Boot / Java 21):** Built using Hexagonal Architecture (Ports & Adapters).
    * **LangChain4j Adapter Pattern:** Decouples core AI orchestration logic from specific LLM providers (currently targeting Google Gemini).
    * **pgvector Semantic Search:** Enforces Retrieval-Augmented Generation (RAG) by converting enterprise policies into embedded vector data for context injection.
    * **Idempotency & Concurrency:** Employs pessimistic database row locking (`SELECT FOR UPDATE`) to prevent race conditions when human managers approve AI-generated tool actions.
    * **Dual-Layer Guardrails:** Implements an adversarial prompt injection interceptor layer before passing context to the deterministic LLM routing logic.
* **Frontend (React / Vite / Tailwind):** A stateless, Splitwise-inspired 3-column dashboard bridging the gap between raw data and human oversight via an Axios interceptor pattern.

## ⚙️ Prerequisites

Ensure the following runtimes are installed on your host machine:
* Docker & Docker Compose
* Java 21 (for local development)
* Node.js 20+ (for local development)

### Environment Setup
Create a `.env` file in the root directory (alongside `docker-compose.yml`) and insert your Gemini API Key:
```env
GEMINI_API_KEY=your_actual_api_key_here
```

## 🚀 Running the Application

### Option A: Full Stack via Docker
To spin up the entire ecosystem (Database, Backend, and Frontend Nginx proxy) with a single command:
1. Ensure your `.env` file is populated.
2. Run: `docker compose up --build`
3. Access the UI at: `http://localhost`
4. Use credentials `agent` / `trustdesk123` to log in.

### Option B: Local Development (Isolated Mode)
If you are modifying code and want hot-reloading for the frontend and fast restarts for Spring Boot:
1. Start only the vector database:
   ```bash
   docker compose up -d db
   ```
2. **Start Backend:** Navigate to `/backend`, ensure your `GEMINI_API_KEY` is set in your IDE or terminal, and run:
   ```bash
   ./mvnw spring-boot:run
   ```
3. **Start Frontend:** Navigate to `/frontend` and run:
   ```bash
   npm run dev
   ```
   *(Starts on port 5173 with an internal Vite proxy routing `/api` traffic to 8080).*

## 🗄️ Data Ingestion & Seeding

TrustDesk requires baseline policies and seed tickets to function. Once the application is running, click the **"Load Seed Data"** button in the left sidebar of the React UI.

Alternatively, execute a direct cURL command:
```bash
curl -X POST http://localhost:8080/api/v1/data/load
```
This populates the PostgreSQL database with operational tickets and calculates 768-dimensional embeddings for the Knowledge Base documents via the Gemini Embedding Model.

## 🔌 API Reference

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/v1/auth/login` | `POST` | Validates credentials and returns a Bearer token. |
| `/api/v1/data/load` | `POST` | Ingests JSON seed data and vectorizes KB documents. |
| `/api/v1/tickets` | `GET` | Retrieves the active operational queue. |
| `/api/v1/tickets/{id}/context` | `GET` | Retrieves rich context (Customer, Order, Ticket state). |
| `/api/v1/tickets/{id}/triage` | `POST` | Triggers the AI routing engine to categorize and prioritize. |
| `/api/v1/tickets/{id}/draft-reply`| `POST` | Triggers the RAG pipeline to generate a grounded reply and propose deterministic tool actions. |
| `/api/v1/tool-actions/{id}/approve` | `POST` | Executes a high-risk tool action (e.g., Refund). |
| `/api/v1/tool-actions/{id}/reject` | `POST` | Rejects a pending high-risk tool action. |
| `/api/v1/eval/run` | `POST` | Executes the LangChain4j batch evaluation runner. |

## 📊 Automated LLM Evaluation Pipeline

TrustDesk includes an automated Evaluation Runner to benchmark LLM accuracy and safety against a baseline dataset (`eval_cases.jsonl`) to prevent regressions when tuning prompts or switching models.

Triggering `/api/v1/eval/run` executes the AI orchestration pipeline against historical test cases and measures:
1. **Triage Accuracy:** Category and Priority match rate.
2. **Citation Coverage:** Verifies the AI successfully appended policy IDs to outputs.
3. **Unsafe Action Block Rate:** Ensures the LLM does not hallucinate forbidden capabilities.
4. **Escalation Behavior:** Measures precise routing for adversarial prompt injection and safety hazards.

## 🔐 Security & Trade-offs

* **Current State:** The REST API utilizes a lightweight Bearer Token Filter (`ApiTokenFilter`) combined with a simulated frontend login flow using `sessionStorage` and Axios interceptors.
* **Enterprise Context:** In a production microservice architecture, this application is designed to sit behind an API Gateway (e.g., Kong, AWS API Gateway). The Gateway handles full OAuth2/OIDC validation and forwards a secure internal service token to Spring Boot. This keeps the service decoupled from heavy RBAC boilerplate while maintaining a zero-trust boundary.

## ⚠️ Known Limitations & Future Scope

1. **Synchronous Evaluation Runner:** The `/eval/run` endpoint currently blocks the HTTP thread while executing API calls sequentially. In a production environment, this would be offloaded to an asynchronous message broker (Kafka/RabbitMQ) and processed by background workers.
2. **Hardcoded Tool Catalog:** The AI is currently restricted to two primary tools (`start_refund_review`, `create_replacement_order`). The Tool Registry enum can be dynamically expanded via a dedicated administration panel in future iterations.