package com.mprataps.varta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

  private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

  @Autowired
  private ChatService chatService;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  // Thread-safe map for concurrent access.
  private final ConcurrentHashMap<String, WebSocketSession> sessionsMap = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
      sessionsMap.put(session.getId(), session);
      log.info("Connected to web socket at {}", session.getId());
      log.info("Total web socket connections: " + sessionsMap.size());

      // send chat history to new client.
      sendChatHistory(session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessionsMap.remove(session.getId());
    log.info("Disconnected from web socket : {} with status: {}", session.getId(), status);
  }

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

    try{

      log.info("Received message from {}: {}",  session.getId(), message.getPayload());

      // Parse JSON message
      Message chatMessage = objectMapper.readValue(message.getPayload(), Message.class);

      // to do -add message validation later
      Message savedMessage = chatService.addMessage(chatMessage.getUserName(), chatMessage.getContent());

      // broadcast to all clients.
      broadcastMessage(savedMessage);

    }catch(Exception e){
      log.error("Error while handling message from web socket: {}: {}",  session.getId(), e.getMessage());
    }

  }

  private void sendChatHistory(WebSocketSession session){

    try{

      List<Message> messages = chatService.getMessages();
      for(Message msg: messages){

        String json = objectMapper.writeValueAsString(msg);
        session.sendMessage(new TextMessage(json));

      }

    }catch(Exception e){

      log.error("Failed to send chat history to : {}: {}",  session.getId(), e.getMessage());

    }

  }

  public void broadcastMessage(Message message){

    String messageJson;

    try{
      messageJson = objectMapper.writeValueAsString(message);
    }catch(Exception e){

      log.error("Failed to serialized message: {}", e.getMessage());
      return;
    }

    // Broadcast to all active sessions while removing inactive sessions
    sessionsMap.entrySet().removeIf(entry -> {

      WebSocketSession session = entry.getValue();

      try{

        if(session.isOpen()){

          session.sendMessage(new TextMessage(messageJson));
          return false; // keep session

        }else{
          return true; // remove clsoed session.
        }


      }catch(IOException e){

        log.warn("Failed to send message to : {}: {}", entry.getKey(), e.getMessage());
        return true; // remove problematic session.
      }



    });

  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    log.error("Error while handling message from web socket: {}: {}",  session.getId(), exception.getMessage());
    sessionsMap.remove(session.getId());
  }


}
