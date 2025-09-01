package com.mprataps.varta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PollingChatClient {

  private static final String BASE_URL = "http://localhost:8080/api";
  private static final int POLL_INTERVAL_SECONDS = 10;

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final Scanner scanner;
  private final ScheduledExecutorService scheduler;

  private String userName;
  private int lastMessageIndex = 0; // for efficient polling.
  private int errorCount = 0; // counter for errors found during polling.

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

  public PollingChatClient() {

    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    this.scanner = new Scanner(System.in);
    this.scheduler = Executors.newScheduledThreadPool(1);

  }

  public void start() {

    System.out.println("--- Polling Chat Client --- ");

    // Get username
    System.out.print("Enter your user name: ");
    userName = scanner.nextLine().trim();

    if(userName.isEmpty()) {
      System.out.println("Username cannot be empty ");
      return;
    }

    System.out.println("Connected! Type messages (or 'quit' to exit):\"");
    System.out.println("Messages will update every " + POLL_INTERVAL_SECONDS + " seconds\n");

    // start polling for messages
    startPolling();

    // handle user input
    handleUserInput();

    // cleanup
    shutdown();

  }

  private void startPolling() {
    scheduler.scheduleAtFixedRate(() -> {pollForNewMessages(); }, 0, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
  }

  private void pollForNewMessages() {

      try{
        String url = BASE_URL + "/messages?since=" + lastMessageIndex;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        List<Message> messages = objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, Message.class));
        for(Message message : messages) {
         displayMessage(message);  // just to nicely format the message
        }

        lastMessageIndex += messages.size();

        errorCount = 0; // reset during success.


      }catch(Exception e){
        errorCount++;
        if(errorCount % 5 == 1) { // print every 1st, 6th, 11th, 16th error
          System.err.println("Error while polling for new messages " + e.getMessage());
        }
      }

  }

  private void handleUserInput() {
    System.out.println("Commands: 'quit', or type a message");

    String input;
    while((input = scanner.nextLine()) != null) {
      switch (input.trim().toLowerCase()) {
        case "quit" -> {
          return;
        }
        default -> {
          if (!input.trim().isEmpty()) {
            sendMessage(input.trim());
          }
        }
      }
    }

  }

  private void sendMessage(String content) {

    try{
      Message message = new Message(userName, content);
      String jsonBody = objectMapper.writeValueAsString(message);
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(BASE_URL + "/messages"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if(response.statusCode() == 201) {

        System.out.println("Message sent successfully");

      }else{
        throw new RuntimeException("Error while sending a message from " + userName+ " with status code " + response.statusCode());
      }

    }catch(Exception e){
      System.out.println(e.getMessage());
    }

  }

  private void displayMessage(Message message) {
    // Format timestamp to HH:MM:SS using DateTimeFormatter (simpler approach)
    Instant timestamp = message.getTimestamp();
    String timeFormatted = timestamp.atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    
    System.out.println("[%s] Username:%s Content:%s".formatted(timeFormatted, message.getUserName(), message.getContent()));
  }

  private void shutdown() {
    System.out.println("\nShutting down...");
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(20, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
    }
  }

  public static void main(String[] args) {
    new PollingChatClient().start();
  }



}
