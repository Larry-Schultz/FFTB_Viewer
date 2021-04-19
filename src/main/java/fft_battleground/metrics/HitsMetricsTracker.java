package fft_battleground.metrics;

import java.util.UUID;

import com.google.common.cache.Cache;
import fft_battleground.repo.HitsType;

import lombok.Data;

@Data
public class HitsMetricsTracker {
	private HitsType hitsType;
	private Cache<String, String> currentHitsCache;
	
	public HitsMetricsTracker(HitsType hitsType, Cache<String, String> currentHitsCache) {
		this.hitsType = hitsType;
		this.currentHitsCache = currentHitsCache;
	}
	
	/*
	 * Will only add entry if the appropriate hits type is provided
	 */
	public void attemptToAddEntry(HitsType hitsType, String url) {
		if(this.hitsType == HitsType.BOTH || this.hitsType == hitsType) {
			String id = this.generateId(url);
			String value = url != null ? url : "dummyName";
			this.currentHitsCache.put(id, value);
		}
	}
	
	public long getSize() {
		long result = this.currentHitsCache.size();
		return result;
	}
	
	protected String generateId(String url) {
		UUID uuid = null;
		try {
			uuid = url != null ? UUID.fromString(url) : UUID.randomUUID();
		} catch(IllegalArgumentException e) {
			uuid = UUID.randomUUID();
		}
		
		String id = uuid.toString();
		return id;
	}
}
