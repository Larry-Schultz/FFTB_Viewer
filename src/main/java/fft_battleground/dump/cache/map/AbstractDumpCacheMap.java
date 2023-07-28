package fft_battleground.dump.cache.map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fft_battleground.dump.cache.DumpCacheMap;

public class AbstractDumpCacheMap<U,V> implements DumpCacheMap<U, V> {

	protected Map<U, V> cache = new ConcurrentHashMap<>();
	
	@Override
	public V get(U key) {
		return this.cache.get(key);
	}

	@Override
	public void put(U key, V value) {
		this.cache.put(key, value);
	}
	
	@Override
	public void remove(U key) {
		this.cache.remove(key);
	}
	
	@Override
	public boolean containsKey(U key) {
		return this.cache.containsKey(key);
	}

	@Override
	public void bulkLoad(Map<U, V> data) {
		this.cache.putAll(data);
	}

	@Override
	public Map<U, V> getMap() {
		return this.cache;
	}
	
}
