package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.ExpEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpDataProvider {

	private static final String DUMP_HIGH_SCORE_URL = "http://www.fftbattleground.com/fftbg/highscores.txt";
	private static final String DUMP_HIGH_EXP_URL = "http://www.fftbattleground.com/fftbg/highexp.txt";
	private static final String DUMP_HIGH_LAST_ACTIVE_URL = "http://www.fftbattleground.com/fftbg/highdate.txt";
	private static final String DUMP_BOT_URL = "http://www.fftbattleground.com/fftbg/bots.txt";
	
	private static final String DUMP_PORTAIL_URL_FORMAT = "http://www.fftbattleground.com/fftbg/portrait/%s";
	private static final String DUMP_ALLEGIANCE_URL_FORMAT = "http://www.fftbattleground.com/fftbg/allegiance/%s";
	private static final String DUMP_USERSKILLS_URL_FORMAT = "http://www.fftbattleground.com/fftbg/userskills/%s";
	private static final String DUMP_PRESTIGE_URL_FORMAT = "http://www.fftbattleground.com/fftbg/prestige/%s";

	@Autowired
	private DumpResourceManager dumpResourceManager;
	
	@Autowired
	private DumpService dumpService;
	
	public Map<String, Integer> getHighScoreDump() throws DumpException {
		Map<String, Integer> data = new HashMap<>();
		
		Resource resource;
		try {
			resource = new UrlResource(DUMP_HIGH_SCORE_URL);
		} catch (MalformedURLException e) {
			log.error("Error getting high score dump");
			throw new DumpException(e);
		}
		try(BufferedReader highScoreReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			highScoreReader.readLine(); //ignore the header
			
			while((line = highScoreReader.readLine()) != null) {
				Integer position = Integer.valueOf(StringUtils.substringBefore(line, ". "));
				String username = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":")));
				Integer value = Integer.valueOf(StringUtils.replace(StringUtils.substringBetween(line, ": ", "G"), ",", ""));
				data.put(username, value);
				this.dumpService.getLeaderboard().put(username, position);
			}
		} catch (DumpException e) {
			log.error("Error getting high score dump");
			throw e;
		} catch (IOException e) {
			log.error("Error getting high score dump");
			throw new DumpException(e);
		}
		
		return data;
	}
	
	public Pair<Integer, Long> getHighScoreTotal() throws DumpException {
		long globalGil = 0L;
		int totalPlayers = 0;
		
		Resource resource = null;
		try {
			resource = new UrlResource(DUMP_HIGH_SCORE_URL);
		} catch (MalformedURLException e) {
			log.error("Error getting high score total", e);
			throw new DumpException(e);
		}
		try(BufferedReader highScoreReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			highScoreReader.readLine(); //ignore the header
			while((line = highScoreReader.readLine()) != null) {
				totalPlayers++;
				long value = Long.valueOf(StringUtils.replace(StringUtils.substringBetween(line, ": ", "G"), ",", ""));
				globalGil += value;
			}
		} catch (DumpException e) {
			log.error("Error getting high score total", e);
			throw e;
		} catch (IOException e) {
			log.error("Error getting high score total", e);
			throw new DumpException(e);
		}
		
		
		Pair<Integer, Long> globalGilData = new ImmutablePair<>(totalPlayers, globalGil);
		return globalGilData;
	}
	
	public Map<String, ExpEvent> getHighExpDump() throws DumpException {
		Map<String, ExpEvent> data = new HashMap<>();
		Resource resource;
		try {
			resource = new UrlResource(DUMP_HIGH_EXP_URL);
		} catch (MalformedURLException e) {
			log.error("Error reading high exp dump", e);
			throw new DumpException(e);
		}
		try(BufferedReader highScoreReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			highScoreReader.readLine(); //ignore the header
			while((line = highScoreReader.readLine()) != null) {
				Integer position = Integer.valueOf(StringUtils.substringBefore(line, "."));
				String username = StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":"));
				Short level = Short.valueOf(StringUtils.substringBetween(line, "Level ", " (EXP:"));
				Short currentExp = Short.valueOf(StringUtils.substringBetween(line, "(EXP: ", ")"));
				Short remainingExp = (short) (100 - currentExp);
				data.put(username, new ExpEvent(username, level, remainingExp));
				this.dumpService.getExpRankLeaderboardByRank().put(position, username);
				this.dumpService.getExpRankLeaderboardByPlayer().put(username, position);
			}
		} catch (DumpException e) {
			log.error("Error reading high exp dump", e);
			throw e;
		} catch (IOException e) {
			log.error("Error reading high exp dump", e);
			throw new DumpException(e);
		}
		
		return data;
	}
	
	public Map<String, Date> getLastActiveDump() throws DumpException {
		Map<String, Date> data = new HashMap<>();
		Resource resource;
		try {
			resource = new UrlResource(DUMP_HIGH_LAST_ACTIVE_URL);
		} catch (MalformedURLException e) {
			log.error("Error with url for dump high last active page");
			throw new DumpException(e);
		}
		SimpleDateFormat dateFormatter = new SimpleDateFormat(DumpService.dateActiveFormatString);
		try(BufferedReader highDateReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			highDateReader.readLine(); //ignore the header
			while((line = highDateReader.readLine()) != null) {
				String username = StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":"));
				String dateStr = StringUtils.substringAfter(line, ": ");
				Date date = dateFormatter.parse(dateStr);
				data.put(username, date);
			}
		} catch (ParseException|IOException e) {
			log.error("Error reading last active dump data", e);
			throw new DumpException(e);
		} catch(DumpException e) {
			log.error("Error reading last active dump data", e);
			throw e;
		}
		
		return data;
	}
	
	public Set<String> getPlayersForPortraitDump() {
		Set<String> players = this.getPlayerList(DUMP_PORTAIL_URL_FORMAT);
		return players;
	}
	
	public Set<String> getPlayersForAllegianceDump() {
		Set<String> players = this.getPlayerList(DUMP_ALLEGIANCE_URL_FORMAT);
		return players;
	}
	
	public Set<String> getPlayersForPrestigeSkillsDump() {
		Set<String> players = this.getPlayerList(DUMP_PRESTIGE_URL_FORMAT);
		return players;
	}
	
	public Set<String> getPlayersForUserSkillsDump() {
		Set<String> players = this.getPlayerList(DUMP_USERSKILLS_URL_FORMAT);
		return players;
	}
	
	public String getPortraitForPlayer(String player) throws DumpException {
		String playerName = StringUtils.lowerCase(player);
		String portrait = null;
		Resource resource = null;
		try {
			resource = new UrlResource(this.getPlayerResourcePath(DUMP_PORTAIL_URL_FORMAT, playerName));
		} catch (MalformedURLException e) {
			log.error("malformed url for getting portrait data for player {}", e, player);
			throw new DumpException(e);
		}
		try(BufferedReader portraitReader = this.dumpResourceManager.openDumpResource(resource)) {
			portrait = portraitReader.readLine();
			if(StringUtils.contains(portrait, "<!DOCTYPE")) {
				portrait = "";
			}
		} catch (DumpException e) {
			log.error("Error getting portrait data for player {}", e, player);
			throw e;
		} catch (IOException e) {
			log.error("Error getting portrait data for player {}", e, player);
			throw new DumpException(e);
		}
		
		return portrait;
	}
	
	public BattleGroundTeam getAllegianceForPlayer(String player) throws DumpException {
		String playerName = StringUtils.lowerCase(player);
		BattleGroundTeam allegiance = null;
		Resource resource;
		try {
			resource = new UrlResource(this.getPlayerResourcePath(DUMP_ALLEGIANCE_URL_FORMAT, playerName));
		} catch (MalformedURLException e) {
			log.error("Error with allegiance url for player {}", playerName);
			throw new DumpException(e);
		}
		try(BufferedReader portraitReader = this.dumpResourceManager.openDumpResource(resource)) {
			String allegianceStr = portraitReader.readLine();
			if(NumberUtils.isCreatable(allegianceStr)) {
				allegiance = BattleGroundTeam.parse(Integer.valueOf(allegianceStr)); 
			} else {
				log.debug("non numeric allegiance found, for player {} with data {}", player, allegianceStr);
				allegiance = BattleGroundTeam.NONE;
			}
		} catch(IOException e) {
			log.error("error getting allegiance data for player {}", playerName);
			throw new DumpException(e);
		} catch(DumpException e) {
			log.error("error getting allegiance data for player {}", playerName);
			throw e;
		}
		
		return allegiance;
	}
	
	public Set<String> getBots() throws DumpException {
		Set<String> bots = new HashSet<>();
		Resource resource;
		try {
			resource = new UrlResource(DUMP_BOT_URL);
		} catch (MalformedURLException e) {
			log.error("Error with bots url {}", DUMP_BOT_URL);
			throw new DumpException(e);
		}
		try(BufferedReader botReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = StringUtils.trim(StringUtils.lowerCase(line));
				bots.add(cleanedString);
			}
		} catch (DumpException e) {
			log.error("Error accessing bots file", e);
			throw e;
		} catch (IOException e) {
			log.error("Error accessing bots file", e);
			throw new DumpException(e);
		}
		
		return bots;
	}
	
	public List<String> getSkillsForPlayer(String player) throws DumpException {
		List<String> skills = this.getSkills(player, DUMP_USERSKILLS_URL_FORMAT);
		return skills;
	}
	
	public List<String> getPrestigeSkillsForPlayer(String player) throws DumpException {
		List<String> skills = this.getSkills(player, DUMP_PRESTIGE_URL_FORMAT);
		return skills;
	}
	
	protected List<String> getSkills(String player, String urlFormat) throws DumpException {
		List<String> skills = new LinkedList<>();
		Resource resource;
		try {
			resource = new UrlResource(this.getPlayerResourcePath(urlFormat, player));
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
			throw new DumpException(e);
		}
		try(BufferedReader skillReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = skillReader.readLine()) != null) {
				if(line.length() < 50 && !StringUtils.contains(line, "<")) {
					//remove the '~' characters
					line = StringUtils.replace(line, "~", "");
					skills.add(line);
				}
			}
		} catch (IOException e) {
			log.debug("no user skills data for player {}", player);
			throw new DumpException(e);
		}
		
		return skills;
	}
	
	public Set<String> getPlayerList(String url) {
		String cleanedUrl = this.getPlayerListPath(url);
		Set<String> playerList = this.getPlayerListFromUrl(cleanedUrl);
		return playerList;
	}
	
	@SneakyThrows
	protected Set<String> getPlayerListFromUrl(String url) {
		Set<String> players = new HashSet<>();
		Document doc = this.dumpResourceManager.openPlayerList(url);
		
		Elements playerNodes = doc.select("a[href$=txt]");
		for(Element element : playerNodes) {
			String filename = element.attr("href");
			if(StringUtils.isNotBlank(filename)) {
				String player = StringUtils.replace(filename, ".txt", "");
				players.add(player);
			}
			
		}
		
		return players;
	}
	
	protected String getPlayerListPath(String url) {
		String playerListPath = String.format(url, "");
		return playerListPath;
	}
	
	protected String getPlayerResourcePath(String url, String player) {
		String playerResourcePath = String.format(url, player) + ".txt";
		return playerResourcePath;
	}
	
}
