package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;

import lombok.SneakyThrows;

@Component
public class DumpResourceManager {
	
	private static final double RATE_LIMIT = 0.5;
	private RateLimiter limit = RateLimiter.create(RATE_LIMIT);
	
	@SneakyThrows
	public BufferedReader openDumpResource(Resource resource) {
		this.limit.acquire();
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		
		return reader;
	}
}
