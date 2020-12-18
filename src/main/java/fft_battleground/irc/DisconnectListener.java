package fft_battleground.irc;

import java.io.IOException;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.events.TwirkListener;

import fft_battleground.discord.WebhookManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisconnectListener  implements TwirkListener {

	private Twirk twirk;
	private WebhookManager errorWebhookManager;
	private IrcReconnectListener ircReconnectListener;
	
	private static final String errorAvertedMessageFormat = "IRC Disconnect averted with count %o";
	private static final String criticalErrorMessageFormat = "Critical error, unable to reconnect to IRC despite %o attempts";
	
	public DisconnectListener() {}
	
	public DisconnectListener(Twirk twirk, WebhookManager errorWebhookManager, IrcReconnectListener ircReconnectListener) {
		this.twirk = twirk;
		this.errorWebhookManager = errorWebhookManager;
		this.ircReconnectListener = ircReconnectListener;
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
			String message = String.format(criticalErrorMessageFormat, this.ircReconnectListener.getCurrentReconnectCount());
			this.errorWebhookManager.sendShutdownNotice(e, message);
			return;
		} 
		catch (InterruptedException e) {  }
		
		String message = String.format(errorAvertedMessageFormat, this.ircReconnectListener.getCurrentReconnectCount());
		this.errorWebhookManager.sendMessage(message);
		this.ircReconnectListener.clearCount();
	}
	
	@Retryable( value = Exception.class, maxAttempts = 30, backoff = @Backoff(delay = 20 * 1000, multiplier=2), listeners = {"ircReconnectListener"})
	public void retryConnection() throws IOException, InterruptedException {
		log.error("Attempting to reconnect to IRC");
		twirk.connect();
	}
}
