package fft_battleground.dump.cache;

import java.util.Map;

public interface DumpCacheMap<U, V> {
	public V get(U key);
	public void put(U key, V value);
	public void remove(U key);
	public boolean containsKey(U key);
	public void bulkLoad(Map<U, V> data);
	public Map<U,V> getMap();
}
