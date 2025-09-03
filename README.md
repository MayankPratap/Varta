# Varta - AI-Enhanced Real-time Chat Application

Varta is a real-time chat application built with Java that demonstrates different approaches to client-server communication along with AI integration. The project showcases both traditional polling and modern WebSocket-based streaming for message delivery, enhanced with an AI assistant named "Viri."

## Features

- **Multiple Client Types:**
  - Polling-based chat client
  - WebSocket streaming chat client
- **Real-time Communication:** Instant message delivery
- **AI Integration:** Built-in AI assistant named "Viri" that responds to specific queries
- **User-friendly Interface:** Simple command-line interface
- **Robust Architecture:** Built on Spring and Jakarta EE

## Technology Stack

- Java 21+
- Spring MVC
- Spring AI
- Ollama Integration
- Jakarta EE
- WebSockets (Java-WebSocket)
- Jackson for JSON processing
- Maven for dependency management

## Prerequisites

Before running the application, ensure you have the following installed:
- JDK 21 or later
- Maven
- Git
- Ollama - for AI integration (with llama3.2:1b model installed)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/yourusername/varta.git
cd varta
```

### Build the Project

```bash
mvn clean install
```

### Setup Ollama for AI Integration

1. Install Ollama from [ollama.ai](https://ollama.ai)
2. Pull the required model:
```bash
ollama pull llama3.2:1b
```
3. Ensure Ollama is running on http://localhost:11434 (default port)

### Running the Server

The server must be running before any clients can connect.

```bash
# Start the server
mvn spring-boot:run
```

The server will start on port 8080.

### Running the Clients

The application provides two types of clients that demonstrate different communication approaches:

#### Polling Chat Client

This client periodically polls the server for new messages.

```bash
# Run the polling client
./run-client.sh
```

#### Streaming Chat Client

This client maintains a WebSocket connection for real-time message delivery.

```bash
# Run the streaming client
./run-streaming-client.sh
```

## Usage

1. Start the server using the instructions above
2. Run either client using its respective script
3. Enter your username when prompted
4. Start chatting!
5. To interact with the AI assistant "Viri", start your message with following phrases - "Hey Viri" or "Hello Viri" or "Hi Viri" (the AI will respond automatically)
6. Type `/exit` to quit the client application

## AI Integration

Varta includes an AI assistant named "Viri" powered by Spring AI and Ollama:

- Uses the llama3.2:1b model (configurable in application.properties)
- Responds automatically when triggered in chat messages
- AI responses are broadcast to all connected clients
- AI configuration options:
  - Model: llama3.2:1b (default)
  - Temperature: 0.7 (controls creativity of responses)

## Project Structure

- `src/main/java/com/mprataps/varta/` - Main application code
  - `VartaApplication.java` - Spring Boot application entry point
  - `RestChatController.java` - REST endpoints for polling client and AI integration
  - `WebSocketConfig.java` - WebSocket configuration
  - `ai/ViriAIService.java` - AI integration service
- `src/main/java/com/mprataps/varta/client/` - Client implementations
  - `PollingChatClient.java` - Traditional polling client
  - `StreamingChatClient.java` - WebSocket-based streaming client

## API Endpoints

- `GET /api/messages` - Retrieve all messages (used by polling client)
- `POST /api/messages` - Send a new message (triggers AI when appropriate)
- WebSocket endpoint: `ws://localhost:8080/chat` (used by streaming client)

## Configuration

The application can be configured through `application.properties`:

```properties
spring.application.name=Varta
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3.2:1b
spring.ai.ollama.chat.options.temperature=0.7
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Java WebSocket library
- Spring Boot framework
- Spring AI project
- Ollama for local AI models
- All contributors and supporters
