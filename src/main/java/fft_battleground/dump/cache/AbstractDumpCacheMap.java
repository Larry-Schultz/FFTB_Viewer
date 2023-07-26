package fft_battleground.dump.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractDumpCacheMap<U,V> implements DumpCache<U, V> {

	protected Map<U, V> cache = new ConcurrentHashMap<>();
	
	@Override
	public V get(U key) {
		return this.cache.get(key);
	}

	@Override
	public void put(U key, V value) {
		this.put(key, value);
	}

	@Override
	public void bulkLoad(Map<U, V> data) {
		this.bulkLoad(data);
	}

}
