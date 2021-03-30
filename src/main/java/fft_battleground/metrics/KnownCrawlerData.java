package fft_battleground.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class KnownCrawlerData {
	private List<CrawlerDataEntry> knownCrawlers;
	
	public List<Pattern> getPatternFromData() {
		List<Pattern> patterns = new ArrayList<>();
		for(CrawlerDataEntry entry : this.knownCrawlers) {
			Pattern p = Pattern.compile(entry.getPattern());
			patterns.add(p);
		}
		return patterns;
	}
}
