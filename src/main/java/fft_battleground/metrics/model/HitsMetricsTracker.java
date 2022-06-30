package fft_battleground.metrics.model;

import com.google.common.cache.Cache;

import fft_battleground.repo.util.HitsType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class HitsMetricsTracker extends MetricsTracker<String, String> {
	private HitsType hitsType;
	
	public HitsMetricsTracker(HitsType hitsType, Cache<String, String> currentHitsCache) {
		super(currentHitsCache);
		this.hitsType = hitsType;
	}
	
	/*
	 * Will only add entry if the appropriate hits type is provided
	 */
	public void attemptToAddEntry(HitsType hitsType, String url) {
		if(this.hitsType == HitsType.BOTH || this.hitsType == hitsType) {
			String id = this.generateId(url);
			String value = url != null ? url : "dummyName";
			this.getCache().put(id, value);
		}
	}
	
}
