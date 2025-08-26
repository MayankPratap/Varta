package com.mprataps.varta;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RestChatController {

  @Autowired
  private ChatService chatService;
  @Autowired
  private WebSocketHandler webSocketHandler;

  @GetMapping("/messages")
  public ResponseEntity<List<Message> > getMessages(@RequestParam(value = "since", required = false) Integer since) {
    if (since != null) {
      return ResponseEntity.ok(chatService.getMessagesSince(since));
    }
    return ResponseEntity.ok(chatService.getMessages());
  }

  @PostMapping("/messages")
  public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
    // Basic validation
    if (StringUtils.isBlank(message.getUserName()) ||  StringUtils.isBlank(message.getContent())) {
      return ResponseEntity.badRequest().build();
    }
    
    Message createdMessage = chatService.addMessage(message.getUserName(), message.getContent());
    // broadcast this polling client message to all the streaming clients.
    webSocketHandler.broadcastMessage(createdMessage);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
  }


}
