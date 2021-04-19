package fft_battleground.model;

import java.util.Date;

import lombok.Data;

@Data
public class ChatMessage {

    private String channel;
    private String username;
    private String message;
    private Boolean isSubscriber = false;
    
    private Date messageTime;
    
    public ChatMessage() {
    	this.messageTime = new Date();
    }

    public ChatMessage(String message) {
    	this.message = message;
    	this.isSubscriber = false;
    	
    	this.messageTime = new Date();
    }
    
    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
        this.isSubscriber = false;
        
    	this.messageTime = new Date();
    }
    
    public ChatMessage(String username, String message, boolean isSubscriber) {
    	this.username = username;
    	this.message = message;
    	this.isSubscriber = isSubscriber;

    	this.messageTime = new Date();
    }

	public ChatMessage(String channel, String userName, String content, boolean sub) {
		this.username = userName;
		this.message = content;
		this.channel = channel;
		this.isSubscriber = sub;
		
		this.messageTime = new Date();
	}

}