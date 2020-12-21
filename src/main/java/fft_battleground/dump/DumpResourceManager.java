package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;

import fft_battleground.exception.DumpException;
import fft_battleground.util.BattlegroundRetryState;
import lombok.SneakyThrows;

@Component
public class DumpResourceManager {
	
	@Autowired
	private DumpResourceRetryManager dumpResourceRetryManager;
	
	private static final double RATE_LIMIT = 0.5;
	private RateLimiter limit = RateLimiter.create(RATE_LIMIT);
	
	public BufferedReader openDumpResource(Resource resource) throws DumpException {
		this.limit.acquire();
		final BattlegroundRetryState state = new BattlegroundRetryState();
		BufferedReader reader = this.dumpResourceRetryManager.openConnection(resource, state);
		
		return reader;
	}
}
