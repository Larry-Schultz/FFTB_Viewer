package fft_battleground.dump.reports;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.Getter;

public abstract class ReportGenerator<T> {
	
	@Getter protected Cache<String, T> cache;
	
	public ReportGenerator(BattleGroundCacheEntryKey key) {
		this.cache = buildCache(key);
	}
	
	public abstract T getReport() throws CacheMissException;
	public abstract T writeReport();
	public abstract T generateReport() throws CacheBuildException;
	
	public T readCache(Cache<String, T> cache, String key) {
		T result = null;
		synchronized(cache) {
			result = cache.getIfPresent(key);
		}
		
		return result;
	}
	
	public void writeToCache(Cache<String, T> cache, String key, T value) {
		synchronized(cache) {
			cache.put(key, value);
		}
		
		return;
	}
	
	private static <T> Cache<String, T> buildCache(BattleGroundCacheEntryKey key) {
		Cache<String, T> cache = Caffeine.newBuilder().expireAfterWrite(key.getTimeValue(), key.getTimeUnit()).maximumSize(1).build();
		return cache;
	}
}
