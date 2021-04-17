package fft_battleground.irc;

import java.io.IOException;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.gikk.twirk.Twirk;

import fft_battleground.exception.IrcConnectionException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IrcInitialConnectManager {

	@Retryable( value = IrcConnectionException.class, maxAttempts = 30, backoff = @Backoff(delay = 20 * 1000, multiplier=2))
	public boolean connectToIrc(Twirk ircChatBot) throws IrcConnectionException {
        boolean startupSuccessful;
		try {
			startupSuccessful = ircChatBot.connect();
		} catch (IOException | InterruptedException e) {
			log.error("Error starting connection to IRC", e);
			throw new IrcConnectionException("Error starting connection to IRC", e);
		}
        if(!startupSuccessful) {
        	log.error("Error starting connection to IRC");
        	throw new IrcConnectionException("twitch irc connection permanently closed");
        }
        
        return startupSuccessful;
	}
}
