package fft_battleground.dump.cache.set;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import fft_battleground.dump.cache.DumpCacheSet;

public class AbstractDumpCacheSet<T> implements DumpCacheSet<T> {
	protected Set<T> set = new HashSet<T>();

	@Override
	public boolean contains(T obj) {
		return this.set.contains(obj);
	}

	@Override
	public Set<T> getSet() {
		return this.set;
	}
	
	@Override
	public void add(T obj) {
		this.set.add(obj);
	}

	@Override
	public void remove(T obj) {
		this.set.remove(obj);
	}

	@Override
	public void reload(Collection<T> data) {
		this.set = new HashSet<>(data);
	}

	@Override
	public int size() {
		return this.set.size();
	}
	
}
