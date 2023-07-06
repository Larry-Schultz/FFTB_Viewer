package fft_battleground.reports;

import com.github.benmanes.caffeine.cache.Cache;

import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;

public interface ReportGenerator<T> {
	T generateReport() throws CacheBuildException;
	T deserializeJson(String json);
	void scheduleUpdates();
	T getReport() throws CacheMissException;
	T readCache(Cache<String, T> cache, String key);
	T writeReport();
	void writeToCache(Cache<String, T> cache, String key, T value);
	T loadFromDatabase();
}
