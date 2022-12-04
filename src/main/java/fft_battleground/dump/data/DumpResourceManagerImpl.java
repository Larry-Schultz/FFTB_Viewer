package fft_battleground.dump.data;

import java.io.BufferedReader;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;

import fft_battleground.exception.DumpException;
import fft_battleground.util.BattlegroundRetryState;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpResourceManagerImpl implements DumpResourceManager {

	@Autowired
	private DumpResourceRetryManager dumpResourceRetryManager;
	
	private Double rateLimit;
	private RateLimiter limit;
	
	public DumpResourceManagerImpl(@Value("${dumpRateLimit}") Double rateLimit) {
		this.rateLimit = rateLimit;
		this.limit = RateLimiter.create(this.rateLimit);
	}
	
	@Override
	public BufferedReader openDumpResource(Resource resource) throws DumpException {
		this.limit.acquire();
		final BattlegroundRetryState state = new BattlegroundRetryState();
		BufferedReader reader = this.dumpResourceRetryManager.openConnection(resource, state);
		
		return reader;
	}
	
	@Override
	public Document openPlayerList(String url) throws DumpException {
		this.limit.acquire();
		final BattlegroundRetryState state = new BattlegroundRetryState();
		Document doc = this.dumpResourceRetryManager.openPlayerList(url, state);
		return doc;
	}
	

}
