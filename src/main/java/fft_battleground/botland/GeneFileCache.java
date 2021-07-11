package fft_battleground.botland;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.botland.model.ResultData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneFileCache {
	private static final long DEFAULT_CACHE_DURATION = 5L;
	
	private Cache<String, ResultData> geneFileCache;
	
	public GeneFileCache() {
		this.geneFileCache = this.buildCache(DEFAULT_CACHE_DURATION);
	}
	
	public GeneFileCache(Long cacheDuration) {
		this.geneFileCache = this.buildCache(cacheDuration);
	}
	
	public ResultData getGeneData(String filename) {
		ResultData data = this.geneFileCache.getIfPresent(filename);
		if(data == null) {
			data = this.loadGeneDataFromFile(filename);
			this.geneFileCache.put(filename, data);
		}
		
		return data;
	}
	
	private ResultData loadGeneDataFromFile(String filename) {
		URL resourceUrl = this.getClass().getClassLoader().getResource(filename);
		ObjectMapper mapper = new ObjectMapper();
		ResultData genes = null;
		try {
			genes = mapper.readValue(resourceUrl, ResultData.class);
		} catch (IOException e) {
			log.error("Error initializing gene bot from file {}", filename, e);
		}
		
		return genes;
	}
	
	private Cache<String, ResultData> buildCache(Long duration) {
		Cache<String, ResultData> cache = Caffeine.newBuilder()
		  .expireAfterWrite(duration, TimeUnit.MINUTES)
		  .maximumSize(1)
		  .build();
		
		return cache;
	}
}
