package fft_battleground.metrics;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.discord.WebhookManager;
import fft_battleground.repo.model.Hits;
import fft_battleground.repo.repository.HitsRepo;
import fft_battleground.repo.util.HitsType;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccessTracker extends Thread {
	private static final Logger accessLogger = LoggerFactory.getLogger("accessLog");

	private static final List<String> robotPatterns = Arrays.asList(new String[] { "msnbot", "googlebot", "petalbot", "crawl" });
	private static final boolean hitsEnabled = true;

	@Autowired
	private HitsRepo hitsRepo;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private WebhookManager errorWebhookManager;

	private BlockingQueue<AccessEntry> accessEntryQueue = new LinkedBlockingQueue<>();
	private List<HitsMetricsTracker> metricsTrackers = new ArrayList<>();

	@PostConstruct
	public void postConstruct() throws IOException, URISyntaxException {
		this.setUpMetrics();
	}

	protected void setUpMetrics() {
		MetricsTrackerBuilder builder = new MetricsTrackerBuilder(this.meterRegistry);
		this.metricsTrackers = Arrays.asList(new HitsMetricsTracker[] {
			builder.buildMetricsTracker(HitsType.BOTH, TimeUnit.HOURS, 24, "fft_viewer.hits.totalDaily", "Total Hits from Crawlers and Users"),
			builder.buildMetricsTracker(HitsType.USER, TimeUnit.HOURS, 24, "fft_viewer.hits.userDaily", "Total daily user hits"),
			builder.buildMetricsTracker(HitsType.CRAWLER, TimeUnit.HOURS, 24, "fft_viewer.hits.crawlerDaily", "Total daily crawler hits"),
			builder.buildMetricsTracker(HitsType.BOTH, TimeUnit.HOURS, 1, "fft_viewer.hits.pastHourTotalHits", "Total hits from the past hour"),
			builder.buildMetricsTracker(HitsType.USER, TimeUnit.HOURS, 1, "fft_viewer.hits.pastHourUserHits", "Total hits from users from the past hour")
		});
	}

	public void addAccessEntry(String pageName, String userAgent, HttpServletRequest request) {
		this.accessEntryQueue.add(new AccessEntry(pageName, userAgent, request));
	}

	@Override
	public void run() {
		while (true) {
			try {
				AccessEntry entry = this.accessEntryQueue.take();
				HitsType hitsType = HitsType.USER;
				boolean isCrawler = false;
				String pageName = entry.getPage();
				isCrawler = this.isUrlACrawler(entry.getRequest());
				if (isCrawler) {
					hitsType = HitsType.CRAWLER;
				}
				this.logAccess(pageName, entry.getRequest(), hitsType);
				this.incrementGuages(entry.getRequest(), hitsType);
				
				// increment hits now
				if (hitsEnabled) {
					this.incrementHits(hitsType);
				}
			} catch (InterruptedException e) {
				log.error("logAccess function interrupted", e);
			}
		}
	}

	@SneakyThrows
	public void logAccess(String pageName, HttpServletRequest request, HitsType urlType) {
		String host = this.getHostnameFromRequest(request);
		accessLogger.info("{} page accessed from {}: {} with hostname {}", pageName, request.getRemoteAddr(), urlType,
				host);
	}
	
	public void incrementGuages(HttpServletRequest request, HitsType urlType) {
		for(HitsMetricsTracker tracker : this.metricsTrackers) {
			String host;
			try {
				host = this.getHostnameFromRequest(request);
			} catch (UnknownHostException e) {
				host = null;
			}
			tracker.attemptToAddEntry(urlType, host);
		}
	}

	@Transactional
	protected Hits incrementHits(HitsType type) {
		Hits hits = null;
		try {
			hits = this.hitsRepo.incrementTodaysHits(type);
		} catch (Exception e) {
			String typeString = type != null ? type.toString() : "NONE";
			log.error("Error updating {} hits count", typeString, e);
		}

		return hits;
	}

	protected boolean isUrlACrawler(HttpServletRequest request) {
		boolean isCrawler = false;

		try {
			String host = this.getHostnameFromRequest(request);
			for (int i = 0; i < robotPatterns.size() && !isCrawler; i++) {
				String currentPattern = robotPatterns.get(i);
				isCrawler = StringUtils.contains(host, currentPattern);
			}
		} catch (UnknownHostException e) {
			// if the dns entry can't be found, its almost certainly not a crawler
			isCrawler = false;
		}

		return isCrawler;
	}
	
	protected String getHostnameFromRequest(HttpServletRequest request) throws UnknownHostException {
		InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
		String host = addr.getHostName();
		
		return host;
	}

}
