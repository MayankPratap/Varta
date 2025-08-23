package com.mprataps.varta;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private String userName; 
    private String content; 
    private Instant timestamp;

    public Message(String userName, String content) {
        this.userName = userName;
        this.content = content;
        this.timestamp = Instant.now();
    }

}
