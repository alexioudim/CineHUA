# Spring Boot Application Setup

This document provides the necessary steps to set up and run the Spring Boot application.

## Setup Instructions

### 1. Start the Application Using Docker Compose
To start the application with all its dependencies, execute the following command:

```bash
docker-compose up
```
This will build and start the required containers for the application.

### 2. Set Up Stripe Webhook

Run the following command to start the Stripe CLI and forward webhook events to your local application:

stripe listen --forward-to localhost:8080/stripe/webhook

This will listen for incoming Stripe events and forward them to the webhook endpoint in your application. Ensure that the webhook secret in the CLI command matches the one defined in your application.properties file.

### 3. Configure application.properties

Open the src/main/resources/application.properties file and add your API keys for external services (OMDb, TMDb, Stripe).

OMDb API Key
omdb.api.key=your_omdb_api_key

TMDb API Key
tmdb.api.key=your_tmdb_api_key

Stripe API Key
stripe.api.key=your_stripe_api_key

Webhook Secret Key
stripe.webhook.secret=your_stripe_webhook_secret

Make sure to replace your_omdb_api_key, your_tmdb_api_key, your_stripe_api_key, and your_stripe_webhook_secret with the actual API keys you have obtained from the respective services.

### 4. Run the Spring Boot Application

Now you can run the Spring Boot application. Execute the following command:

./mvnw spring-boot:run

The application will start and be accessible at http://localhost:8080.
