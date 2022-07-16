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

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.exception.BotConfigException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GeneFileCache<T> {
	
	protected Pair<Date, String> genefileWithMostRecentData;
	
	public GeneFileCache() {}
	
	protected abstract Class<T> getCacheType();
	protected abstract String baseFolder();
	
	public T getGeneData(String filename) throws BotConfigException {
		GeneFile<T> geneFile = this.loadGeneDataFromFile(filename);
		
		T data = (T) geneFile.getData();
		if(this.genefileWithMostRecentData == null || this.genefileWithMostRecentData.getLeft().before(geneFile.getFileDate())) {
			this.genefileWithMostRecentData = Pair.of(geneFile.getFileDate(), geneFile.getFilename());
		}
		return data;
	}
	
	public T getLatestFile() throws BotConfigException {
		T result = this.genefileWithMostRecentData != null ? this.getGeneData(this.genefileWithMostRecentData.getRight()) : null; 
		
		return result;
	}
	
	public GeneFile<T> loadGeneDataFromFile(String filename) throws BotConfigException {
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
