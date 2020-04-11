package fft_battleground.irc;

import java.util.concurrent.BlockingQueue;

import org.pircbotx.PircBotX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fft_battleground.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IrcChatMessenger extends Thread {
	
	@Autowired
	private PircBotX ircChatBot;
	
    @Value("${irc.channel}")
    private String channel;
    
    @Autowired
    private BlockingQueue<ChatMessage> ircChatMessengerQueue;
    
    public IrcChatMessenger() {
    	this.setName(this.getClass().getName());
    }
    
    @Override
    public void run() {
    	while(true) {
    		try {
				ChatMessage message = this.ircChatMessengerQueue.take();
				log.info("Sending chat message: {}", message.getMessage());
				this.ircChatBot.sendIRC().message("#"+this.channel, message.getMessage());
			} catch (InterruptedException e) {
				log.error("Error with the IrcChatMessengerQueue", e);
			}
    	}
    }

}
