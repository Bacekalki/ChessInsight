# ♟️ ChessInsight

**ChessInsight** is a chess game analytics application built with Java and Spring Boot. It allows players to import their games from Chess.com and Lichess, analyze their performance, and evaluate their games using Stockfish.

The project combines chess data processing, backend development, chess engine integration, and an AI-generated frontend into one application.

## Features

- **Direct Game Import** – Import games directly from Chess.com or Lichess by entering a username.
- **PGN Import** – Upload PGN files to import chess games.
- **Game History** – Browse imported games through a paginated REST API.
- **Performance Analytics** – Track total games, wins, losses, draws, and win rate.
- **Opening Analysis** – Classify openings and explore performance across different openings.
- **Stockfish Integration** – Analyze chess positions and games using the Stockfish chess engine.
- **Database Persistence** – Store chess games and their metadata in PostgreSQL.
- **Frontend** – A user interface generated using OpenCode and integrated with the Spring Boot backend.

## Technologies Used

### Backend
- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Chesslib – PGN parsing
- Stockfish – Chess engine analysis
- MapStruct – DTO mapping
- Lombok
- JUnit 5 and Mockito – Testing

### Frontend
- Generated using **OpenCode**
- Communicates with the backend through REST APIs

## Architecture

The backend follows a layered architecture:

- **Controller** – Handles HTTP requests and responses.
- **Service** – Contains business logic and coordinates application operations.
- **Repository** – Handles database operations using Spring Data JPA.
- **Entity** – Represents the database model.
- **DTO** – Defines the data exposed through the API.
- **Mapper** – Converts entities into DTOs using MapStruct.

## Data Sources and Integrations

- **Chess.com API** – Retrieve a player's publicly available games using their username.
- **Lichess API** – Retrieve a player's games using their username.
- **Stockfish** – Evaluate chess positions and provide engine analysis.
- **Chesslib** – Parse PGN files and process chess games.

## API Endpoints

Base URL: `http://localhost:8080`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/games/import` | Import games from a PGN file |
| GET | `/api/games` | Retrieve paginated game history |
| GET | `/api/analytics/summary?username={username}` | Retrieve performance statistics |

## Database

ChessInsight uses PostgreSQL to persist imported chess games and their metadata.

Create a database named:

```sql
CREATE DATABASE chessinsight;
```

The database connection is configured through environment variables.

## Running the Project

1. Clone the repository.
2. Make sure Java 21 and PostgreSQL are installed.
3. Create the `chessinsight` database.
4. Configure the database password through the `DB_PASSWORD` environment variable.
5. Run the application:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend will start at:

```text
http://localhost:8080
```

## Project Motivation

ChessInsight was created to combine software engineering with chess, transforming raw game data into meaningful performance insights.

The project demonstrates practical experience with Spring Boot, RESTful API development, relational database design, external API integration, chess engine integration, and frontend-backend communication.

## Acknowledgements

- [Chesslib](https://github.com/bhlangonijr/chesslib) – Chess PGN parsing library.
- [Stockfish](https://stockfishchess.org/) – Open-source chess engine.
- [Chess.com](https://www.chess.com/) and [Lichess](https://lichess.org/) – Chess game data sources.
- **OpenCode** – Used to generate the frontend.
