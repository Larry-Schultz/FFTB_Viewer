package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;

import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import fft_battleground.exception.DumpException;
import fft_battleground.util.BattlegroundRetryState;

@Component
public class DumpResourceRetryManager {

	@Retryable( value = DumpException.class, maxAttempts = 10, backoff = @Backoff(delay = 2000, multiplier=3))
	protected BufferedReader openConnection(Resource resource, BattlegroundRetryState state) throws DumpException {
		state.incrementCount();
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		} catch (IOException e) {
			final String messageFormat = "Dump reconnection failed for url {0}: attempt {1}";
			String url = null;
			try {
				url = resource.getURL().toString();
			} catch (IOException e1) {}
			String message = MessageFormat.format(messageFormat, url, state.getRetryCount());
			throw new DumpException(e, message);
		}
		
		return reader;
	}
}
