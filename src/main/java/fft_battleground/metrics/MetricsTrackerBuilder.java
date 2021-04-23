package fft_battleground.metrics;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fft_battleground.repo.HitsType;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

public class MetricsTrackerBuilder {
	
	private MeterRegistry meterRegistry;
	
	public MetricsTrackerBuilder(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}
	
	public HitsMetricsTracker buildMetricsTracker(HitsType hitsType, TimeUnit timeUnit, long duration, String guageName, String guageDescription) {
		Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(duration, timeUnit).build();
		HitsMetricsTracker tracker = new HitsMetricsTracker(hitsType, cache);
		
		Gauge.builder(guageName, tracker, HitsMetricsTracker::getSize).description(guageDescription).register(this.meterRegistry); 
		
		return tracker;
	}
}
