package com.mprataps.varta.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class StreamingChatClient {

	private static final String WEBSOCKET_URL = "ws://localhost:8080/chat";
	private WebSocketClient client;
	private final ObjectMapper mapper;
	private final Scanner scanner;
	private String userName;
	private final CountDownLatch connectionLatch = new CountDownLatch(1);

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class Message{

		private String userName;
		private String content;
		private Instant timestamp;
		Message(String userName, String content) {
			this.userName = userName;
			this.content = content;
			this.timestamp = Instant.now();
		}
	}

	public StreamingChatClient() {

		this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		this.scanner = new Scanner(System.in);

	}

	public void start() {

		System.out.println("=== Streaming Chat Client (WebSocket) ===");

		// Get username.
		System.out.print("Enter your username: ");
		userName = scanner.nextLine().trim();
		if(userName.isEmpty()) {
			System.out.println("Username cannot be empty");
			return;
		}

		// connect to Websocket
		connectToWebSocket();

		// wait for connection
		try{

			System.out.println("Connecting..");
			connectionLatch.await();
		}catch(InterruptedException e){
			System.out.println("Connection was interrupted.");
			return;
		}

		System.out.println("Connected! Type messages (or 'quit' to exit):");
		System.out.println("Messages will appear instantly!\n");

		// handle user input.
		handleUserInput();

		//cleanup
		shutDown();

	}

	private void connectToWebSocket(){

		try{

			URI serverUri = new URI(WEBSOCKET_URL);
			client = new WebSocketClient(serverUri) {

				@Override
				public void onOpen(ServerHandshake handshake) {

					System.out.println("✅ Connected to chat server!");
					System.out.println("Server HTTP Status: " + handshake.getHttpStatus());
					connectionLatch.countDown();
				}

				@Override
				public void onMessage(String message){

					try{

						Message msg = mapper.readValue(message, Message.class);
						displayMessage(msg);

					} catch (Exception e) {

						System.out.println("Error while parsing the message: " + e.getMessage());

					}
				}

				@Override
				public void onClose(int code, String reason, boolean remote){

					String source = remote? "server" : "client";
					System.out.println("🔌 Disconnected by " + source +
					                   " (Code: " + code + ", Reason: " + reason + ")");

					connectionLatch.countDown();

				}

				@Override
				public void onError(Exception e){

					System.err.println("❌ WebSocket error: " + e.getMessage());
					connectionLatch.countDown();

				}

			};

			client.connect();

		}catch(Exception e){

			System.err.println("Failed to connect to websocket: " + e.getMessage());
			connectionLatch.countDown();
		}

	}

	private void handleUserInput(){

		String input;

		while((input = scanner.nextLine()) != null) {

			if ("quit".equalsIgnoreCase(input.trim())) {
				break;
			}

			if (!input.trim().isEmpty()) {
				sendMessage(input.trim());
			}
		}
	}

	private void sendMessage(String content){

		Message msg = new Message(userName, content);
		try {
			String jsonMessage = mapper.writeValueAsString(msg);
			if(isConnected()) {
				client.send(jsonMessage);
			}else{
				System.out.println("Client is not connected!");
			}
		} catch (JsonProcessingException e) {
			System.err.println("Error while converting message to json string: " + e.getMessage());
		}
	}

	private boolean isConnected() {

		if(client!=null && !client.isClosed()){
			return true;
		}
		return false;
	}

	private void displayMessage(Message message){

		// Format timestamp to HH:MM:SS using DateTimeFormatter (simpler approach)
		Instant timestamp = message.getTimestamp();
		String timeFormatted = timestamp.atZone(java.time.ZoneId.systemDefault())
				.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

		System.out.println("[%s] Username:%s Content:%s".formatted(timeFormatted, message.getUserName(), message.getContent()));

	}

	private void shutDown(){

		System.out.println("\nDisconnecting...");

		try{
			if(isConnected()) {
				client.close();
			}
		}catch(Exception e){
			System.err.println("Error while disconnecting: " + e.getMessage());
		}

		System.out.println("Goodbye!");

	}

	public static void main(String[] args) {
		new StreamingChatClient().start();
	}
}
