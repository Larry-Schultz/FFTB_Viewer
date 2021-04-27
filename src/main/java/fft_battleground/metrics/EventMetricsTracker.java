package fft_battleground.metrics;

import com.google.common.cache.Cache;

import fft_battleground.event.model.BattleGroundEvent;

public class EventMetricsTracker<T extends BattleGroundEvent> extends MetricsTracker<String, T> {

	public EventMetricsTracker(Cache<String, T> cache) {
		super(cache);
		// TODO Auto-generated constructor stub
	}

}
