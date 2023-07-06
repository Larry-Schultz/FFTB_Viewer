package fft_battleground.reports;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.discord.WebhookManager;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractReportGenerator<T> implements ReportGenerator<T> {
	
	public static final int HIGHEST_PLAYERS = 10;
	public static final int TOP_PLAYERS = 100;
	public static final int PERCENTILE_THRESHOLD = 10;
	
	@Getter protected Cache<String, T> cache;
	@Getter protected BattleGroundCacheEntryKey key;
	@Getter protected String reportName;
	@Getter protected BattleGroundCacheEntryRepo battleGroundCacheEntryRepo;
	@Getter protected WebhookManager errorWebhookManager;
	@Getter protected Timer battleGroundCacheTimer;
	
	public AbstractReportGenerator(BattleGroundCacheEntryKey key, String reportName, BattleGroundCacheEntryRepo battleGroundCacheEntryRepo,
			WebhookManager errorWebhookManager, Timer battleGroundCacheTimer) {
		this.cache = buildCache(key);
		this.key = key;
		this.reportName = reportName;
		this.battleGroundCacheEntryRepo = battleGroundCacheEntryRepo;
		this.errorWebhookManager = errorWebhookManager;
		this.battleGroundCacheTimer = battleGroundCacheTimer;
	}
	
	@Override
	public abstract T generateReport() throws CacheBuildException;
	@Override
	public abstract T deserializeJson(String json);
	
	@Override
	public void scheduleUpdates() {
		AbstractReportGenerator<T> generator = this;
		this.battleGroundCacheTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				generator.writeReport();
			}
		}, 5 * 60 * 1000, key.millis());
	}
	
	@Override
	public T getReport() throws CacheMissException {
		T report = this.readCache(this.cache, key.getKey());
		if (report == null) {
			throw new CacheMissException(key);
		}

		return report;
	}
	
	@Override
	public T readCache(Cache<String, T> cache, String key) {
		T result = null;
		synchronized(cache) {
			result = cache.getIfPresent(key);
		}
		
		return result;
	}
	
	@Override
	public T writeReport() {
		log.warn(this.reportName + " cache was busted, creating new value");
		T report = null;
		try {
			report = this.generateReport();
			this.writeToCache(this.cache, key.getKey(), report);
			this.getBattleGroundCacheEntryRepo().writeCacheEntry(report, key.getKey());
		} catch(Exception e) {
			log.error("Error writing to " + this.reportName + " cache", e);
			this.getErrorWebhookManager().sendException(e, "exception generating new " + this.reportName);
		}
		
		log.warn(getReportName() + " rebuild complete");
		
		return report;
	}
	
	@Override
	public void writeToCache(Cache<String, T> cache, String key, T value) {
		synchronized(cache) {
			cache.put(key, value);
		}
		
		return;
	}
	
	private static <T> Cache<String, T> buildCache(BattleGroundCacheEntryKey key) {
		Cache<String, T> cache = Caffeine.newBuilder().expireAfterWrite(key.getTimeValue() * 2, key.getTimeUnit()).maximumSize(1).build();
		return cache;
	}
	
	protected T readBattleGroundCacheEntryRepo() {
		T result = null;
		try {
			result = this.getBattleGroundCacheEntryRepo().readCacheEntry(key, this::deserializeJson);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public T loadFromDatabase() {
		log.info("Searching for " + this.reportName + " data from database cache");
		T report = this.readBattleGroundCacheEntryRepo();
		if(report != null) {
			log.info("Loading " + this.reportName + " data from database cache");
			this.getCache().put(key.getKey(), report);
		} else {
			log.info(this.reportName + " data from database cache not found");
		}
		
		return report;
	}
	
	@SuppressWarnings("unchecked")
	@SneakyThrows
	protected Map<Integer, Double> deserializeMapIntegerDouble(String str) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Double> stringDoubleMap = mapper.readValue(str,  Map.class);
		
		Map<Integer, Double> result = new HashMap<>();
		for(Map.Entry<String, Double> stringDoubleMapEntry: stringDoubleMap.entrySet()) {
			Integer intKey = Integer.valueOf(stringDoubleMapEntry.getKey());
			result.put(intKey, stringDoubleMapEntry.getValue());
		}
		return result;
	}
}
