package com.mprataps.varta.ai;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service 
public class ViriAIService {
	private final OllamaChatModel chatModel;
	private final ChatClient chatClient;
	public ViriAIService(OllamaChatModel chatModel) {
		this.chatModel = chatModel;
		this.chatClient = ChatClient.create(chatModel);
	}
	public String chat(String content) {
		return chatClient.prompt().user(content).call().content();
	}

	public boolean isAIInvoked(String content) {
		if (content == null) {
			return false;
		}
		return content.startsWith("Hey Viri") || content.startsWith("Hello Viri") || content.startsWith("Hi Viri");
	}

}
