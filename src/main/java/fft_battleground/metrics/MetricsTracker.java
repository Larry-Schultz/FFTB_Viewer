package fft_battleground.metrics;

import java.util.UUID;

import com.google.common.cache.Cache;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MetricsTracker<T,V> {
	protected Cache<T, V> cache;
	
	public MetricsTracker(Cache<T, V> cache) {
		this.cache = cache;
	}
	
	protected Cache<T, V> getCache() {
		Cache<T, V> cache = this.cache;
		return cache;
	}
	
	public long getSize() {
		long result = this.getCache().size();
		return result;
	}
	
	protected String generateId(String contentString) {
		UUID uuid = null;
		try {
			uuid = contentString != null ? UUID.fromString(contentString) : UUID.randomUUID();
		} catch(IllegalArgumentException e) {
			uuid = UUID.randomUUID();
		}
		
		String id = uuid.toString();
		return id;
	}
}
