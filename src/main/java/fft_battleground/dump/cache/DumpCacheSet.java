package fft_battleground.dump.cache;

import java.util.Collection;
import java.util.Set;

public interface DumpCacheSet<T> {
	boolean contains(T obj);
	Set<T> getSet();
	void add(T obj);
	void remove(T obj);
	void reload(Collection<T> data);
	int size();
}
