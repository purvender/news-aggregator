# News Aggregator

A backend service for aggregating news from multiple APIs into a single platform. This application fetches news articles from various sources, processes them, and provides them via an API for consumption by other services or clients.

## Features

- Aggregates news articles from various external APIs.
- Allows searching and filtering news based on categories, keywords, and more.
- Supports thumbnail generation for news images.
- Provides endpoints for accessing aggregated news in JSON format.

## Technology Stack

- **Spring Boot** - For backend development and API endpoints.
- **PostgreSQL** - Relational database for storing news articles.
- **Lombok** - To reduce boilerplate code.
- **Quartz** - For scheduling periodic tasks (like fetching new articles).
- **Thumbnailator** - For image processing and thumbnail generation.
- **JUnit** - For unit and integration tests.
- **Maven** - For project management and dependency handling.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing
We welcome contributions! Please fork the repository, create a new branch, and submit a pull request with your changes.

