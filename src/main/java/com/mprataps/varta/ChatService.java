package com.mprataps.varta;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatService {

    private final List<Message> messages = new CopyOnWriteArrayList<>();

    public List<Message> getMessages() {

      return new ArrayList<>(messages);

    }

    public List<Message> getMessagesSince(int lastIndex){

      if(lastIndex>=messages.size()){
        return new ArrayList<>();
      }

      return messages.subList(lastIndex, messages.size());

    }

    public Message addMessage(String userName, String content) {

      Message message = new Message(userName, content);
      messages.add(message);
      return message;
    }

    public int getMessageCount() {
      return messages.size();
    }

}
