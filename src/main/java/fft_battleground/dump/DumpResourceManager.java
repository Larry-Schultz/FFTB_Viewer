package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
	
	private static final double RATE_LIMIT = 0.5;
	private RateLimiter limit = RateLimiter.create(RATE_LIMIT);
	
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
	
	public Set<String> getPlayersActiveAfterDate(String url, Date lastActive) throws DumpException {
		Set<PlayerListData> data = this.getPlayerDataFromList(url);
		Set<String> players = data.parallelStream()
				.filter(playerData -> playerData.getLastUpdated().compareTo(lastActive) > 0)
				.map(playerData -> playerData.getPlayerName())
				.collect(Collectors.toSet());
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
}
