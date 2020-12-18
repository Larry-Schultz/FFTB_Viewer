package fft_battleground.irc;

import java.io.IOException;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.events.TwirkListener;

import fft_battleground.discord.WebhookManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisconnectListener  implements TwirkListener{

	private Twirk twirk;
	private WebhookManager errorWebhookManager;
	
	public DisconnectListener() {}
	
	public DisconnectListener(Twirk twirk, WebhookManager errorWebhookManager) {
		this.twirk = twirk;
		this.errorWebhookManager = errorWebhookManager;
	}
	
	@Override
	public void onDisconnect() {
		//Twitch might sometimes disconnects us from chat. If so, try to reconnect. 
		try { 
			this.retryConnection();
		} 
		catch (IOException e) { 
			//If reconnection threw an IO exception, close the connection and release resources.
			twirk.close();
			this.errorWebhookManager.sendShutdownNotice(e, "Critical error, unable to communicate with IRC");
			return;
		} 
		catch (InterruptedException e) {  }
		
		this.errorWebhookManager.sendMessage("IRC Disconnect averted");
	}
	
	@Retryable( value = Exception.class, maxAttempts = 30, backoff = @Backoff(delay = 20 * 1000, multiplier=2))
	public void retryConnection() throws IOException, InterruptedException {
		log.error("Attempting to reconnect to IRC");
		twirk.connect();
	}
}
