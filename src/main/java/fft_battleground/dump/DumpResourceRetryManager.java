package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
			final String messageFormat = "Dump recoonection failed: attempt %o";
			String message = String.format(messageFormat, state.getRetryCount());
			throw new DumpException(e, message);
		}
		
		return reader;
	}
}
