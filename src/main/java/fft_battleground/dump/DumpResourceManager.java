package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;

import fft_battleground.exception.DumpException;
import lombok.SneakyThrows;

@Component
public class DumpResourceManager {
	
	private static final double RATE_LIMIT = 0.5;
	private RateLimiter limit = RateLimiter.create(RATE_LIMIT);
	
	public BufferedReader openDumpResource(Resource resource) throws DumpException {
		this.limit.acquire();
		BufferedReader reader = this.openConnection(resource);
		
		return reader;
	}
	
	@Retryable( value = DumpException.class, maxAttempts = 10, backoff = @Backoff(delay = 2000, multiplier=3))
	protected BufferedReader openConnection(Resource resource) throws DumpException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		return reader;
	}
}
