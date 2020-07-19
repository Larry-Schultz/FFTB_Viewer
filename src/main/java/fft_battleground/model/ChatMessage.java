package fft_battleground.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
public class ChatMessage {

    private String channel;
    private String username;
    private String message;
    private Boolean isSubscriber = false;

    public ChatMessage(String message) {
    	this.message = message;
    	this.isSubscriber = false;
    }
    
    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
        this.isSubscriber = false;
    }
    
    public ChatMessage(String username, String message, boolean isSubscriber) {
    	this.username = username;
    	this.message = message;
    	this.isSubscriber = isSubscriber;
    }

}