package fft_battleground.botland.bot.genetic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.exception.BotConfigException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GeneFileCache<T> {
	private static final long DEFAULT_CACHE_DURATION = 5L;
	
	private Cache<String, GeneFile<T>> geneFileCache;
	
	public GeneFileCache() {
		this.geneFileCache = this.buildCache(DEFAULT_CACHE_DURATION);
	}
	
	public GeneFileCache(Long cacheDuration) {
		this.geneFileCache = this.buildCache(cacheDuration);
	}
	
	protected abstract Class<T> getCacheType();
	protected abstract String baseFolder();
	
	public T getGeneData(String filename) throws BotConfigException {
		GeneFile<T> geneFile = this.geneFileCache.getIfPresent(filename);
		if(geneFile == null) {
			geneFile = this.loadGeneDataFromFile(filename);
			this.geneFileCache.put(filename, geneFile);
		}
		
		T data = (T) geneFile.getData();
		return data;
	}
	
	public T getLatestFile() {
		List<GeneFile<T>> geneFiles = new ArrayList<>(this.geneFileCache.asMap().values());
		Collections.sort(geneFiles, Collections.reverseOrder());
		Optional<GeneFile<T>> firstEntry = geneFiles.stream().findFirst();
		
		T data = null;
		if(firstEntry.isPresent()) {
			data = firstEntry.get().getData();
		}
		return data;
	}
	
	protected GeneFile<T> loadGeneDataFromFile(String filename) throws BotConfigException {
		String path = baseFolder() + "/" + filename;
		URL resourceUrl = this.getClass().getClassLoader().getResource(path);
		File resourceFile = new File(filename);
		Date modificationDate = new Date(resourceFile.lastModified());
		ObjectMapper mapper = new ObjectMapper();
		T genes = null;
		try {
			genes = (T) mapper.readValue(resourceUrl, this.getCacheType());
		} catch (IOException e) {
			String errorMessage = "Error initializing gene bot from file " + filename;
			log.error(errorMessage);
			throw new BotConfigException(errorMessage);
		}
		
		GeneFile<T> result = new GeneFile<>(filename, modificationDate, genes);
		
		return result;
	}
	
	private Cache<String, GeneFile<T>> buildCache(Long duration) {
		Cache<String, GeneFile<T>> cache = Caffeine.newBuilder()
		  .expireAfterWrite(duration, TimeUnit.MINUTES)
		  .maximumSize(1)
		  .build();
		
		return cache;
	}
}
