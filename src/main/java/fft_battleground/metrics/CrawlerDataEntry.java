package fft_battleground.metrics;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlerDataEntry {
	private String pattern;
	private String url;
	private List<String> instances;
	
	@JsonProperty("addition_date")
	private String additionDate;
}
