package fft_battleground.botland;

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

import fft_battleground.botland.model.ResultData;
import fft_battleground.exception.BotConfigException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneFileCache {
	private static final long DEFAULT_CACHE_DURATION = 5L;
	
	private Cache<String, GeneFile> geneFileCache;
	
	public GeneFileCache() {
		this.geneFileCache = this.buildCache(DEFAULT_CACHE_DURATION);
	}
	
	public GeneFileCache(Long cacheDuration) {
		this.geneFileCache = this.buildCache(cacheDuration);
	}
	
	public ResultData getGeneData(String filename) throws BotConfigException {
		GeneFile geneFile = this.geneFileCache.getIfPresent(filename);
		if(geneFile == null) {
			geneFile = this.loadGeneDataFromFile(filename);
			this.geneFileCache.put(filename, geneFile);
		}
		
		ResultData data = geneFile.getData();
		return data;
	}
	
	public ResultData getLatestFile() {
		List<GeneFile> geneFiles = new ArrayList<>(this.geneFileCache.asMap().values());
		Collections.sort(geneFiles, Collections.reverseOrder());
		Optional<GeneFile> firstEntry = geneFiles.stream().findFirst();
		
		ResultData data = null;
		if(firstEntry.isPresent()) {
			data = firstEntry.get().getData();
		}
		return data;
	}
	
	private GeneFile loadGeneDataFromFile(String filename) throws BotConfigException {
		URL resourceUrl = this.getClass().getClassLoader().getResource(filename);
		File resourceFile = new File(filename);
		Date modificationDate = new Date(resourceFile.lastModified());
		ObjectMapper mapper = new ObjectMapper();
		ResultData genes = null;
		try {
			genes = mapper.readValue(resourceUrl, ResultData.class);
		} catch (IOException e) {
			String errorMessage = "Error initializing gene bot from file " + filename;
			log.error(errorMessage);
			throw new BotConfigException(errorMessage);
		}
		
		GeneFile result = new GeneFile(filename, modificationDate, genes);
		
		return result;
	}
	
	private Cache<String, GeneFile> buildCache(Long duration) {
		Cache<String, GeneFile> cache = Caffeine.newBuilder()
		  .expireAfterWrite(duration, TimeUnit.MINUTES)
		  .maximumSize(1)
		  .build();
		
		return cache;
	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class GeneFile implements Comparable<GeneFile> {
	private String filename;
	private Date fileDate;
	private ResultData data;
	
	@Override
	public int compareTo(GeneFile o) {
		return this.getFileDate().compareTo(o.getFileDate());
	}
}
