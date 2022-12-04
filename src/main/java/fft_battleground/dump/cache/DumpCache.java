package fft_battleground.dump.cache;

import java.util.Map;

public interface DumpCache<U, V> {
	public V get(U key);
	public void put(U key, V value);
	public void bulkLoad(Map<U, V> data);
}
