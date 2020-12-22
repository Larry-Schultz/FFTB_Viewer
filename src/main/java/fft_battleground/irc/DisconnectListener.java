package fft_battleground.irc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.events.TwirkListener;

import fft_battleground.discord.WebhookManager;
import fft_battleground.exception.IrcConnectionException;
import fft_battleground.util.BattlegroundRetryState;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DisconnectListener  implements TwirkListener {

	@Autowired
	private Twirk twirk;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private IrcReconnectManager ircReconnectManager;
	
	private static final String errorAvertedMessageFormat = "IRC Disconnect averted with count %o";
	private static final String criticalErrorMessageFormat = "Critical error, unable to reconnect to IRC despite %o attempts";
	
	public DisconnectListener() {}
	
	public DisconnectListener(Twirk twirk, WebhookManager errorWebhookManager) {
		this.twirk = twirk;
		this.errorWebhookManager = errorWebhookManager;
	}
	
	@Override
	public void onDisconnect() {
		//Twitch might sometimes disconnects us from chat. If so, try to reconnect. 
		final BattlegroundRetryState state = new BattlegroundRetryState();
		try { 
			this.ircReconnectManager.retryConnection(this.twirk, state);
		} 
		catch (IrcConnectionException e) { 
			//If reconnection threw an IO exception, close the connection and release resources.
			twirk.close();
			String message = String.format(criticalErrorMessageFormat, state.getRetryCount());
			log.error(message, e);
			this.errorWebhookManager.sendShutdownNotice(e, message);
			return;
		}
		
		String message = String.format(errorAvertedMessageFormat, state.getRetryCount());
		this.errorWebhookManager.sendMessage(message);
	}
	
}
