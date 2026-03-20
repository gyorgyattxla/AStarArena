# A* Arena

A competitive algorithmic coding platform where engineers write custom Java heuristics to solve complex Sokoban maps using the A* (A-Star) search algorithm. 

Submissions are compiled and executed in an isolated Docker-in-Docker (DinD) Alpine sandbox, evaluated for efficiency (Nodes Expanded) and speed (Execution Time), and ranked on a global leaderboard.

## Features

* **Real-time Code Editor:** Integrated Monaco Editor (VS Code engine) for writing Java 21 heuristics directly in the browser with full syntax highlighting.
* **Asynchronous Evaluation:** Submissions are queued via RabbitMQ and processed by background Spring Boot workers to prevent HTTP blocking and timeouts.
* **Secure Sandbox Execution:** User-submitted code is injected into a pre-built Java template and run inside an ephemeral, network-disabled Alpine Docker container.
* **Dynamic Map Management:** Users can upload custom ASCII-based Sokoban levels to challenge the global algorithms.
* **Global Leaderboard:** Ranks heuristics based on the lowest number of nodes expanded, using execution time as a tie-breaker.

## Architecture & Tech Stack

The platform is designed as a modern, distributed microservices architecture.

**Frontend:**
* React 18 + Vite
* Tailwind CSS v4
* `@monaco-editor/react` for code input
* `@tanstack/react-query` for asynchronous state management and polling
* Nginx (for production static file serving and React Router history fallback)

**Backend & Infrastructure:**
* Java 21 / Spring Boot 3
* Spring Data JPA / Hibernate
* MySQL 8.0 (Relational Database)
* RabbitMQ (Message Broker for async task queuing)
* Docker Compose (Orchestration)

## Prerequisites

To run this application locally, you must have the following installed on your host machine:
* [Docker](https://docs.docker.com/get-docker/)
* [Docker Compose](https://docs.docker.com/compose/install/)

## Local Setup & Installation

The entire application (Database, Message Queue, Backend, and Frontend) is containerized and managed via a single Docker Compose file.

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/yourusername/astar-arena.git](https://github.com/yourusername/astar-arena.git)
   cd astar-arena
   ```

2. **Build and spin up the infrastructure:**
   ```bash
   docker-compose up --build -d
   ```
   
3. **Access the application:**
  * Frontend UI: `http://localhost:5173`
  * Backend API: `http://localhost:8080`
  * RabbitMQ Management UI: `http://localhost:15672` (guest/guest)

## License

This project is licensed under the MIT License.
