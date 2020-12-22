package fft_battleground.irc;

import java.io.IOException;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.gikk.twirk.Twirk;

import fft_battleground.exception.IrcConnectionException;
import fft_battleground.util.BattlegroundRetryState;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IrcReconnectManager {

	@Retryable( value = IrcConnectionException.class, maxAttempts = 30, backoff = @Backoff(delay = 20 * 1000, multiplier=2))
	public void retryConnection(final Twirk twirk, final BattlegroundRetryState state) throws IrcConnectionException {
		log.error("Attempting to reconnect to IRC");
		state.incrementCount();
		try {
			twirk.connect();
		} catch (IOException | InterruptedException e) {
			throw new IrcConnectionException(e);
		}
		
	}
}
