package fft_battleground.bot;

import java.util.TimerTask;

import fft_battleground.model.ChatMessage;
import fft_battleground.util.Router;

public class MessageSenderTask extends TimerTask {
	private Router<ChatMessage> messageSenderRouterRef;
	private String message;
	
	public MessageSenderTask(Router<ChatMessage> routerRef, String message) {
		this.message = message;
		this.messageSenderRouterRef = routerRef;
	}

	@Override
	public void run() {
		this.messageSenderRouterRef.sendDataToQueues(new ChatMessage(this.message));
	}
}