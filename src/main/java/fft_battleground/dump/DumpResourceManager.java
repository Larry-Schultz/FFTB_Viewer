package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.RateLimiter;

import fft_battleground.dump.model.PlayerListData;
import fft_battleground.exception.DumpException;
import fft_battleground.util.BattlegroundRetryState;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpResourceManager {

	@Autowired
	private DumpResourceRetryManager dumpResourceRetryManager;
	
	private Double rateLimit;
	private RateLimiter limit;
	
	public DumpResourceManager(@Value("${dumpRateLimit}") Double rateLimit) {
		this.rateLimit = rateLimit;
		this.limit = RateLimiter.create(this.rateLimit);
	}
	
	public BufferedReader openDumpResource(Resource resource) throws DumpException {
		this.limit.acquire();
		final BattlegroundRetryState state = new BattlegroundRetryState();
		BufferedReader reader = this.dumpResourceRetryManager.openConnection(resource, state);
		
		return reader;
	}
	
	public Document openPlayerList(String url) throws DumpException {
		this.limit.acquire();
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		return doc;
	}
	
	public Set<String> getAllPlayersFromList(String url) throws DumpException {
		Set<PlayerListData> playerList = this.getPlayerDataFromList(url);
		Set<String> players = playerList.parallelStream().map(playerListData -> playerListData.getPlayerName()).collect(Collectors.toSet());
		return players;
	}
	
	public Set<String> walkPlayerList(String url) throws DumpException {
		Pair<Integer, Integer> initialTimeUnitTimeLengthPair = new ImmutablePair<>(Calendar.HOUR, 1);
		Pair<Integer, Integer> walkTimeUnitTimeLengthPair = new ImmutablePair<>(Calendar.MINUTE, 10);
		Set<String> players = this.getRecentlyUpdatedPlayers(url, initialTimeUnitTimeLengthPair, walkTimeUnitTimeLengthPair);
		return players;
	}
	
	public Set<String> getRecentlyUpdatedPlayers(String url, Pair<Integer, Integer> initialTimeUnitTimeLengthPair, Pair<Integer, Integer> walkTimeUnitTimeLengthPair) throws DumpException {
		List<PlayerListData> entriesWithNullLastUpdated = new ArrayList<>();
		List<PlayerListData> playerList = this.getPlayerDataFromList(url).stream()
				.peek(playerListData -> {
					if(playerListData.getLastUpdated() == null) {
						entriesWithNullLastUpdated.add(playerListData);
					}
				}).filter(playerListData -> playerListData.getLastUpdated() != null)
				.sorted(Collections.reverseOrder())
				.collect(Collectors.toList());
		
		if(entriesWithNullLastUpdated.size() > 0) {
			log.warn("There were {} players with null recently updated dates for url {}", entriesWithNullLastUpdated.size(), url);
		}
		
		List<PlayerListData> recentPlayers = new ArrayList<>();
		PlayerListData previous = null;
		for(PlayerListData playerData: playerList) {
			TimeZone etTimeZone = TimeZone.getTimeZone("America/New_York");
			Calendar pastTime = Calendar.getInstance(etTimeZone);
			if(previous != null) { //use the default for the first time, and the previous entry's last updated every other time
				pastTime.setTime(previous.getLastUpdated());
				pastTime.add(walkTimeUnitTimeLengthPair.getLeft(), (-1) * walkTimeUnitTimeLengthPair.getRight());
			} else {
				pastTime.add(initialTimeUnitTimeLengthPair.getLeft(), (-1) * initialTimeUnitTimeLengthPair.getRight());
			}
			if(pastTime.getTime().compareTo(playerData.getLastUpdated()) < 0) {
				previous = playerData;
				recentPlayers.add(playerData);
			} else {
				break;
			}
		}
		
		Set<String> players = recentPlayers.parallelStream().map(playerListData -> playerListData.getPlayerName()).collect(Collectors.toSet());
		return players;
	}
	
	public Set<PlayerListData> getPlayerDataFromList(String url) throws DumpException {
		Set<PlayerListData> playerList = new HashSet<>();
		Document doc = this.openPlayerList(url);
		
		Elements playerNodes = doc.select("a[href$=txt]");
		for(Element element : playerNodes) {
			String player = null;
			Date lastUpdatedDate = null;
			String filename = element.attr("href");
			if(StringUtils.isNotBlank(filename)) {
				player = StringUtils.replace(filename, ".txt", "");
			}
			
			List<Element> getAllNeighboringNodes = element.parent().parent().children();
			for(Element tableElement : getAllNeighboringNodes) {
				if(tableElement.hasText()) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						lastUpdatedDate = sdf.parse(tableElement.text());
					} catch(ParseException e) {
						//do nothing, just keep looping.  Since its perfectly valid to get here
					}
				}
			}
			PlayerListData data = new PlayerListData(player, lastUpdatedDate);
			playerList.add(data);
		}
		
		return playerList;
	}
	
	/**
	 * generates a UrlResource from given path
	 * @param path
	 * @return
	 * @throws DumpException
	 */
	public Resource getUrlResource(String path) throws DumpException {
		Resource resource;
		try {
			resource = new UrlResource(path);
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
			throw new DumpException(e);
		}
		
		return resource;
	}
}
