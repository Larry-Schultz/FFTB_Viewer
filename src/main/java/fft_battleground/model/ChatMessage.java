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

    public ChatMessage(String message) {
    	this.message = message;
    }
    
    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
    }

}