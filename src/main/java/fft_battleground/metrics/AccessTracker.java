package fft_battleground.metrics;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.discord.WebhookManager;
import fft_battleground.repo.HitsType;
import fft_battleground.repo.model.Hits;
import fft_battleground.repo.repository.HitsRepo;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccessTracker extends Thread {
	private static final Logger accessLogger = LoggerFactory.getLogger("accessLog");

	private static final String knownCrawlerUrlPatternFile = "knownCrawlerPatterns.json";
	private static final String knownRobotUserAgentRegexFilename = "RobotUserAgentRegex.txt";
	
	@Autowired
	private HitsRepo hitsRepo;
	
	@Autowired
	private MeterRegistry meterRegistry;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	private BlockingQueue<AccessEntry> accessEntryQueue = new LinkedBlockingQueue<>();
	
	private Pattern knownCrawlerUrlPatterns;
	
	private Hits todayKnownCrawlerHits;
	private Hits todayUserHits;
	
	private Gauge totalDailyHitGauge;
	private Gauge userDailyHitGauge;
	private Gauge crawlerDailyHitGauge;
	
	@PostConstruct 
	public void postConstruct() throws IOException, URISyntaxException {
		//this.setUpMetrics();
		//this.parseKnownCrawlerUrlPatterns();
		//this.loadTodaysHits();
	}
	
	
	protected void setUpMetrics() {
		this.totalDailyHitGauge = Gauge.builder("fft_viewer.hits.totalDaily", this, AccessTracker::getTotalHits).description("Total Hits from Crawlers and Users").register(this.meterRegistry);
		this.userDailyHitGauge = Gauge.builder("fft_viewer.hits.userDaily", this.todayUserHits, Hits::getTotal).description("Total daily user hits").register(this.meterRegistry);
		this.crawlerDailyHitGauge = Gauge.builder("fft_viewer.hits.crawlerDaily", this.todayKnownCrawlerHits, Hits::getTotal).description("Total daily crawler hits").register(this.meterRegistry);
	}
	
	protected void parseKnownCrawlerUrlPatterns() throws IOException, URISyntaxException {
		try {
			String regex = null;
			
			URL classPathKnownRobotUserAgentRegexFilenameUrl = ClassLoader.getSystemResource(knownRobotUserAgentRegexFilename);
			if(classPathKnownRobotUserAgentRegexFilenameUrl != null) {
				Path fileName = Path.of(classPathKnownRobotUserAgentRegexFilenameUrl.toURI());
				regex = Files.readString(fileName);
			}
			else {
				Path fileName = Path.of("/home/pi/FFT_Battleground/" + knownRobotUserAgentRegexFilename);
				regex = Files.readString(fileName);
			}
			 this.knownCrawlerUrlPatterns = Pattern.compile(regex);
		} catch (IOException e) {
			String errorMessage = "Error reading known crawler url pattern file";
			log.error(errorMessage, e);
			this.errorWebhookManager.sendException(e, errorMessage);
			throw e;
		} catch(PatternSyntaxException e) {
			String errorMessage = "Error compiling regex";
			log.error(errorMessage, e);
			this.errorWebhookManager.sendException(e, errorMessage);
		}
		
		return;
	}
	
	protected void loadTodaysHits() {
		Map<HitsType, Hits> hitsMap = this.hitsRepo.getTodaysHits();
		this.todayKnownCrawlerHits = hitsMap.get(HitsType.CRAWLER);
		this.todayUserHits = hitsMap.get(HitsType.USER);
	}
	
	public void addAccessEntry(String pageName, String userAgent, HttpServletRequest request) {
		this.accessEntryQueue.add(new AccessEntry(pageName, userAgent, request));
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				AccessEntry entry = this.accessEntryQueue.take();
				HitsType hitsType = HitsType.USER;
				boolean isCrawler = false;
				String pageName = entry.getPage();
				isCrawler = this.isUrlACrawler(entry.getUserAgent(), entry.getRequest().getRemoteAddr());
				if(isCrawler) {
					hitsType = HitsType.CRAWLER;
				}
				this.logAccess(pageName, entry.getRequest(), hitsType);
				
				//increment hits now
				if(isCrawler) {
					this.incrementTodaysCrawlerHits();
				} else {
					this.incrementTodaysUserHits();
				}
			} catch (InterruptedException e) {
				log.error("logAccess function interrupted", e);
			}
		}
	}
	
	@SneakyThrows
	public void logAccess(String pageName, HttpServletRequest request, HitsType urlType) {
		InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
		String host = addr.getHostName();
		accessLogger.info("{} page accessed from {}: {} with hostname {}", pageName, request.getRemoteAddr(), urlType, host);
	}
	
	@Transactional
	protected Hits incrementTodaysUserHits() {
		Hits result = null;
		String todaysString = this.hitsRepo.getTodaysDateString();
		if(!StringUtils.equals(this.todayUserHits.getDay(), todaysString)) {
			Hits newUserHits = new Hits(HitsType.USER, todaysString, 1);
			this.hitsRepo.saveAndFlush(newUserHits);
			result = newUserHits;
		} else {
			this.todayUserHits.setTotal(this.todayUserHits.getTotal() + 1);
			this.hitsRepo.saveAndFlush(this.todayUserHits);
			result = this.todayUserHits;
		}
		return result;
	}
	
	@Transactional
	protected Hits incrementTodaysCrawlerHits() {
		Hits result = null;
		String todaysString = this.hitsRepo.getTodaysDateString();
		if(!StringUtils.equals(this.todayKnownCrawlerHits.getDay(), todaysString)) {
			Hits newUserHits = new Hits(HitsType.CRAWLER, todaysString, 1);
			this.hitsRepo.saveAndFlush(newUserHits);
			result = newUserHits;
		} else {
			this.todayKnownCrawlerHits.setTotal(this.todayKnownCrawlerHits.getTotal() + 1);
			this.hitsRepo.saveAndFlush(this.todayKnownCrawlerHits);
			result = this.todayKnownCrawlerHits;
		}
		return result;
	}
	
	protected boolean isUrlACrawler(String userAgent, String ip) {
		boolean result = false;
		if(userAgent != null) {
			Matcher matcher = this.knownCrawlerUrlPatterns.matcher(userAgent);
			result = matcher.matches();
		} else {
			log.warn("user agent is null for ip: {}", ip);
		}
		return result;
	}
	
	public Integer getTotalHits() {
		Integer result = this.todayKnownCrawlerHits.getTotal() + this.todayUserHits.getTotal();
		return result;
	}
}


@NoArgsConstructor
@Data
class AccessEntry {
	private String page;
	private String userAgent;
	private HttpServletRequest request;
	
	private Object urlLStuff;
	
	public String getUrl() throws UnknownHostException {
		InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
		String host = addr.getHostName();
		return host;
	}

	public Object getUrlLStuff() {
		return urlLStuff;
	}

	public void setUrlLStuff(Object urlLStuff) {
		this.urlLStuff = urlLStuff;
	}

	public AccessEntry(String pageName, String userAgent, HttpServletRequest request) {
		this.page = pageName;
		this.userAgent = userAgent;
		this.request = request;
	}

}
