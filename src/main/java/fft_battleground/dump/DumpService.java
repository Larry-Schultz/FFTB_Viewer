package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import fft_battleground.bot.model.BalanceType;
import fft_battleground.bot.model.BalanceUpdateSource;
import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.SkillType;
import fft_battleground.bot.model.event.AllegianceEvent;
import fft_battleground.bot.model.event.BalanceEvent;
import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.ExpEvent;
import fft_battleground.bot.model.event.LastActiveEvent;
import fft_battleground.bot.model.event.OtherPlayerBalanceEvent;
import fft_battleground.bot.model.event.OtherPlayerExpEvent;
import fft_battleground.bot.model.event.PlayerSkillEvent;
import fft_battleground.bot.model.event.PortraitEvent;
import fft_battleground.bot.model.event.PrestigeSkillsEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;
import fft_battleground.util.Router;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@CacheConfig(cacheNames = {"music"})
public class DumpService {

	private static final String DUMP_HIGH_SCORE_URL = "http://www.fftbattleground.com/fftbg/highscores.txt";
	private static final String DUMP_HIGH_EXP_URL = "http://www.fftbattleground.com/fftbg/highexp.txt";
	private static final String DUMP_HIGH_LAST_ACTIVE_URL = "http://www.fftbattleground.com/fftbg/highdate.txt";
	private static final String DUMP_PLAYLIST_URL = "http://www.fftbattleground.com/fftbg/playlist.xml";
	private static final String DUMP_BOT_URL = "http://www.fftbattleground.com/fftbg/bots.txt";
	
	private static final String DUMP_PORTAIL_URL_FORMAT = "http://www.fftbattleground.com/fftbg/portrait/%s.txt";
	private static final String DUMP_ALLEGIANCE_URL_FORMAT = "http://www.fftbattleground.com/fftbg/allegiance/%s.txt";
	private static final String DUMP_USERSKILLS_URL_FORMAT = "http://www.fftbattleground.com/fftbg/userskills/%s.txt";
	private static final String DUMP_PRESTIGE_URL_FORMAT = "http://www.fftbattleground.com/fftbg/prestige/%s.txt";
	
	// Wed Jan 01 00:00:00 EDT 2020
	private static final String dateFormatString = "EEE MMM dd HH:mm:ss z yyyy";
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	private Map<String, Integer> balanceCache = new ConcurrentHashMap<>();
	private Map<String, ExpEvent> expCache = new HashMap<>();
	private Map<String, Date> lastActiveCache = new HashMap<>();
	private Map<String, String> portraitCache = new HashMap<>();
	private Map<String, BattleGroundTeam> allegianceCache = new HashMap<>();
	private Map<String, List<String>> userSkillsCache = new HashMap<>();
	private Map<String, List<String>> prestigeSkillsCache = new HashMap<>();
	private Set<String> botCache;
	
	private Map<String, Integer> leaderboard = new HashMap<>();
	
	
	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	public DumpService() {}
	
	@PostConstruct
	@Transactional
	@SneakyThrows
	private void setUpCaches() {
		log.info("loading player data cache");
		List<PlayerRecord> playerRecords = this.playerRecordRepo.findAll();
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastKnownAmount() == null).forEach(playerRecord -> playerRecord.setLastKnownAmount(GambleUtil.MINIMUM_BET));
		this.balanceCache = new ConcurrentHashMap<>(playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getLastKnownAmount)));
		
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastKnownLevel() == null).forEach(playerRecord -> playerRecord.setLastKnownLevel((short) 1));
		this.expCache = playerRecords.parallelStream().map(playerRecord -> new ExpEvent(playerRecord.getPlayer(), playerRecord.getLastKnownLevel(), playerRecord.getLastKnownRemainingExp()))
							.collect(Collectors.toMap(ExpEvent::getPlayer, Function.identity()));
		
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastActive() == null).forEach(playerRecord -> {
			try {
				SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormatString);
				playerRecord.setLastActive(dateFormatter.parse("Wed Jan 01 00:00:00 EDT 2020"));
			} catch (ParseException e) {
				log.error("error parsing date for lastActive", e);
			}
		});
		this.lastActiveCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getLastActive));
		
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getPortrait() == null).forEach(playerRecord -> playerRecord.setPortrait(""));
		this.portraitCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getPortrait));
		
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getAllegiance() == null).forEach(playerRecord -> playerRecord.setAllegiance(BattleGroundTeam.NONE));
		this.allegianceCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getAllegiance));
		
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getPlayerSkills() == null).forEach(playerRecord -> playerRecord.setPlayerSkills(new ArrayList<>()));
		this.userSkillsCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, 
				playerRecord -> playerRecord.getPlayerSkills().stream().filter(playerSkill -> playerSkill.getSkillType() == SkillType.USER)
				.map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList())
				));
		this.prestigeSkillsCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, 
				playerRecord -> playerRecord.getPlayerSkills().stream().filter(playerSkill -> playerSkill.getSkillType() == SkillType.PRESTIGE)
				.map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList())
				));
		
		this.botCache = this.getBots();
		
		this.getHighScoreDump();
		
		log.info("player data cache load complete");
	}
	
	@Scheduled(cron = "0 0 1 * * ?")
	public Map<String, String> updatePortraits() {
		log.info("updating portrait cache");
		Set<String> playerNamesSet = this.portraitCache.keySet();
		Map<String, String> portraitsFromDump = playerNamesSet.parallelStream().collect(Collectors.toMap(Function.identity(), player -> this.getPortraitForPlayer(player)));
		
		Map<String, ValueDifference<String>> balanceDelta = Maps.difference(this.portraitCache, portraitsFromDump).entriesDiffering();
		List<BattleGroundEvent> portraitEvents = new LinkedList<>();
		for(String key: balanceDelta.keySet()) {
			PortraitEvent event = new PortraitEvent(key, balanceDelta.get(key).rightValue());
			portraitEvents.add(event);
			//update cache with new data
			this.portraitCache.put(key, balanceDelta.get(key).rightValue());
		}
		
		portraitEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(portraitEvents);
		log.info("portrait cache update complete");
		
		return portraitsFromDump;
	}
	
	@Scheduled(cron = "0 15 1 * * ?")
	public Map<String, BattleGroundTeam> updateAllegiances() {
		log.info("updating allegiances cache");
		Set<String> playerNamesSet = this.allegianceCache.keySet();
		Map<String, BattleGroundTeam> allegiancesFromDump = playerNamesSet.parallelStream().collect(Collectors.toMap(Function.identity(), player -> this.getAllegianceForPlayer(player)));
		
		Map<String, ValueDifference<BattleGroundTeam>> balanceDelta = Maps.difference(this.allegianceCache, allegiancesFromDump).entriesDiffering();
		List<BattleGroundEvent> allegianceEvents = new LinkedList<>();
		for(String key: balanceDelta.keySet()) {
			AllegianceEvent event = new AllegianceEvent(key, balanceDelta.get(key).rightValue());
			allegianceEvents.add(event);
			//update cache with new data
			this.allegianceCache.put(key, balanceDelta.get(key).rightValue());
		}
		
		allegianceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(allegianceEvents);
		log.info("allegiances cache update complete.");
		
		return allegiancesFromDump;
	}
	
	@Scheduled(cron = "0 30 1 * * ?")
	public Map<String, List<String>> updateUserSkills() {
		log.info("updating user skills cache");
		Set<String> playerNamesSet = this.userSkillsCache.keySet();
		Map<String, List<String>> userSkillsFromDump = playerNamesSet.parallelStream().collect(Collectors.toMap(Function.identity(), player -> this.getSkillsForPlayer(player)));
		
		Map<String, List<String>> differences = this.userSkillsCache.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), 
													key -> ListUtils.<String>subtract(userSkillsFromDump.get(key), this.userSkillsCache.get(key))));
		List<BattleGroundEvent> skillEvents = new ArrayList<>();
		for(String key : differences.keySet()) {
			if(differences.get(key).size() > 0) {
				skillEvents.add(new PlayerSkillEvent(key, differences.get(key)));
			}
		}
		
		skillEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(skillEvents);
		log.info("user skills cache update complete");
		
		return userSkillsFromDump;
	}
	
	@Scheduled(cron = "0 45 1 * * ?")
	public Map<String, List<String>> updatePrestigeSkills() {
		log.info("updating prestige skills cache");
		Set<String> playerNamesSet = this.userSkillsCache.keySet();
		Map<String, List<String>> prestigeSkillsFromDump = playerNamesSet.parallelStream().collect(Collectors.toMap(Function.identity(), player -> this.getPrestigeSkillsForPlayer(player)));
		
		Map<String, List<String>> differences = this.userSkillsCache.keySet().parallelStream().collect(Collectors.toMap(Function.identity(), 
													key -> ListUtils.<String>subtract(prestigeSkillsFromDump.get(key), this.prestigeSkillsCache.get(key))));
		List<BattleGroundEvent> skillEvents = new ArrayList<>();
		for(String key : differences.keySet()) {
			if(differences.get(key).size() > 0) {
				skillEvents.add(new PrestigeSkillsEvent(key, differences.get(key)));
			}
		}
		
		skillEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(skillEvents);
		log.info("user skills cache update complete");
		
		return prestigeSkillsFromDump;
	}
	
	@Scheduled(cron = "0 0 2 * * ?")
	public Set<String> updateBotList() {
		log.info("updating bot list");
		
		Set<String> dumpBots = this.getBots();
		dumpBots.stream().forEach(botName -> this.botCache.add(botName));
		
		log.info("bot list update complete");
		return this.botCache;
	}
	
	@SneakyThrows
	public Map<String, Integer> getHighScoreDump() {
		Map<String, Integer> data = new HashMap<>();
		
		Resource resource = new UrlResource(DUMP_HIGH_SCORE_URL);
		try(BufferedReader highScoreReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			String line;
			highScoreReader.readLine(); //ignore the header
			
			while((line = highScoreReader.readLine()) != null) {
				Integer position = Integer.valueOf(StringUtils.substringBefore(line, ". "));
				String username = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":")));
				Integer value = Integer.valueOf(StringUtils.replace(StringUtils.substringBetween(line, ": ", "G"), ",", ""));
				data.put(username, value);
				this.leaderboard.put(username, position);
			}
		}
		
		return data;
	}
	
	@SneakyThrows
	public Map<String, ExpEvent> getHighExpDump() {
		Map<String, ExpEvent> data = new HashMap<>();
		Resource resource = new UrlResource(DUMP_HIGH_EXP_URL);
		try(BufferedReader highScoreReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			String line;
			highScoreReader.readLine(); //ignore the header
			while((line = highScoreReader.readLine()) != null) {
				String username = StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":"));
				Short level = Short.valueOf(StringUtils.substringBetween(line, "Level ", " (EXP:"));
				Short exp = Short.valueOf(StringUtils.substringBetween(line, "(EXP: ", ")"));
				data.put(username, new ExpEvent(username, level, exp));
			}
		}
		
		return data;
	}
	
	@SneakyThrows
	public Map<String, Date> getLastActiveDump() {
		Map<String, Date> data = new HashMap<>();
		Resource resource = new UrlResource(DUMP_HIGH_LAST_ACTIVE_URL);
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormatString);
		try(BufferedReader highDateReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			String line;
			highDateReader.readLine(); //ignore the header
			while((line = highDateReader.readLine()) != null) {
				String username = StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":"));
				String dateStr = StringUtils.substringAfter(line, ": ");
				Date date = dateFormatter.parse(dateStr);
				data.put(username, date);
			}
		}
		
		return data;
	}
	
	public String getPortraitForPlayer(String player) {
		String playerName = StringUtils.lowerCase(player);
		String portrait = null;
		Resource resource = null;
		try {
			resource = new UrlResource(String.format(DUMP_PORTAIL_URL_FORMAT, playerName));
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
		}
		try(BufferedReader portraitReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			portrait = portraitReader.readLine();
			if(StringUtils.contains(portrait, "<!DOCTYPE")) {
				portrait = "";
			}
		} catch (IOException e) {
			return ""; //no data could be found
		}
		
		return portrait;
	}
	
	@SneakyThrows
	public BattleGroundTeam getAllegianceForPlayer(String player) {
		String playerName = StringUtils.lowerCase(player);
		BattleGroundTeam allegiance = null;
		Resource resource = new UrlResource(String.format(DUMP_ALLEGIANCE_URL_FORMAT, playerName));
		try(BufferedReader portraitReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			String allegianceStr = portraitReader.readLine();
			if(NumberUtils.isCreatable(allegianceStr)) {
				allegiance = BattleGroundTeam.parse(Integer.valueOf(allegianceStr)); 
			} else {
				log.debug("non numeric allegiance found, for player {} with data {}", player, allegianceStr);
				allegiance = BattleGroundTeam.NONE;
			}
		}
		
		return allegiance;
	}
	
	@SneakyThrows
	public Set<String> getBots() {
		Set<String> bots = new HashSet<>();
		Resource resource = new UrlResource(DUMP_BOT_URL);
		try(BufferedReader botReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = StringUtils.trim(StringUtils.lowerCase(line));
				bots.add(cleanedString);
			}
		}
		
		return bots;
	}
	
	public List<String> getSkillsForPlayer(String player) {
		List<String> skills = this.getSkills(player, DUMP_USERSKILLS_URL_FORMAT);
		return skills;
	}
	
	public List<String> getPrestigeSkillsForPlayer(String player) {
		List<String> skills = this.getSkills(player, DUMP_PRESTIGE_URL_FORMAT);
		return skills;
	}
	
	public Collection<BattleGroundEvent> getUpdatesFromDumpService() {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();
		
		log.info("updating balance cache");
		Map<String, Integer> newBalanceDataFromDump = this.getHighScoreDump();
		Map<String, ValueDifference<Integer>> balanceDelta = Maps.difference(this.balanceCache, newBalanceDataFromDump).entriesDiffering();
		OtherPlayerBalanceEvent otherPlayerBalance= new OtherPlayerBalanceEvent(BattleGroundEventType.OTHER_PLAYER_BALANCE, new ArrayList<BalanceEvent>());
		for(String key: balanceDelta.keySet()) {
			BalanceEvent newEvent = new BalanceEvent(key, balanceDelta.get(key).rightValue(), BalanceType.DUMP, BalanceUpdateSource.DUMP);
			otherPlayerBalance.getOtherPlayerBalanceEvents().add(newEvent);
			//update cache with new data
			this.balanceCache.put(key, balanceDelta.get(key).rightValue());
		}
		data.add(otherPlayerBalance);
		log.info("balance cache update complete");
		
		log.info("updating exp cache");
		Map<String, ExpEvent> newExpDataFromDump = this.getHighExpDump();
		Map<String, ValueDifference<ExpEvent>> expDelta = Maps.difference(this.expCache, newExpDataFromDump).entriesDiffering();
		OtherPlayerExpEvent expEvents = new OtherPlayerExpEvent(new ArrayList<ExpEvent>());
		for(String key: expDelta.keySet()) {
			ExpEvent newEvent = expDelta.get(key).rightValue();
			expEvents.getExpEvents().add(newEvent);
			this.expCache.put(key, newEvent);
		}
		data.add(expEvents);
		log.info("exp cache update complete");
		
		log.info("updating last active cache");
		Map<String, Date> newLastActiveFromDump = this.getLastActiveDump();
		Map<String, ValueDifference<Date>> lastActiveDelta = Maps.difference(this.lastActiveCache, newLastActiveFromDump).entriesDiffering();
		for(String key: lastActiveDelta.keySet()) {
			LastActiveEvent newEvent = new LastActiveEvent(key, lastActiveDelta.get(key).rightValue());
			data.add(newEvent);
			//update cache with new data
			this.lastActiveCache.put(key, lastActiveDelta.get(key).rightValue());
		}
		log.info("last active cache update complete");
		
		return data;
	}
	
	@SneakyThrows
	@Cacheable("music")
	public Collection<Music> getPlaylist() {
		Set<Music> musicSet = new HashSet<>();
		
		Resource resource = new UrlResource(DUMP_PLAYLIST_URL);
		StringBuilder xmlData = new StringBuilder();
		String line;
		try(BufferedReader musicReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			while((line = musicReader.readLine()) != null) {
				xmlData.append(line);
			}
		}
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new StringReader(xmlData.toString())));
		doc.getDocumentElement().normalize();
		
		NodeList leafs = doc.getElementsByTagName("leaf");
		for(int i = 0; i < leafs.getLength(); i++) {
			Node node = leafs.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				String name = element.getAttribute("uri");
				name = StringUtils.substringAfterLast(name, "/");
				name = StringUtils.substringBefore(name, ".mp3");
				name = URLDecoder.decode(name, StandardCharsets.UTF_8.toString());
				musicSet.add(new Music(name, element.getAttribute("id"), element.getAttribute("duration")));
			}
		}
		
		Collection<Music> musicList = musicSet.stream().collect(Collectors.toList()).stream().sorted().collect(Collectors.toList());
		
		
		return musicList;
	}
	
	public Integer getLeaderboardPosition(String player) {
		String lowercasePlayer = StringUtils.lowerCase(player);
		Integer position =  this.leaderboard.get(lowercasePlayer);
		return position;
	}
	
	public Map<String, Integer> getBotLeaderboard() {
		Map<String, Integer> botBalances = new TreeMap<>(this.botCache.parallelStream().filter(botName -> this.balanceCache.containsKey(botName))
				.collect(Collectors.toMap(Function.identity(), bot -> this.balanceCache.get(bot))));
		return botBalances;
	}
	
	public Map<String, Integer> getTopPlayers(Integer count) {
		BiMap<String, Integer> topPlayers = HashBiMap.create();
		topPlayers.putAll(this.leaderboard.keySet().parallelStream().filter(player -> !this.botCache.contains(player))
				.filter(player -> this.playerRecordRepo.findById(StringUtils.lowerCase(player)).isPresent())
				.collect(Collectors.toMap(Function.identity(), player -> this.leaderboard.get(player))));
		Set<Integer> topValues = topPlayers.values().stream().sorted().limit(count).collect(Collectors.toSet());
		
		BiMap<Integer, String> topPlayersInverseMap = topPlayers.inverse();
		Map<String, Integer> leaderboardWithoutBots = topValues.stream().collect(Collectors.toMap(rank -> topPlayersInverseMap.get(rank), Function.identity()));
		return leaderboardWithoutBots;
	}
	
	public Date getLastActiveDateFromCache(String player) {
		Date date = this.lastActiveCache.get(player);
		return date;
	}
	
	public Integer getBalanceFromCache(String player) {
		Integer balance = this.balanceCache.get(player);
		return balance;
	}
	
	public Set<String> getBotNames() {
		Set<String> botNames = this.botCache;
		return botNames;
	}
	
	public TimerTask getDataUpdateTask() {
		return new GenerateDataUpdateFromDump(this.eventRouter, this);
	}
	
	public void updateBalanceCache(BalanceEvent event) {
		this.balanceCache.put(event.getPlayer(), event.getAmount());
	}
	
	protected List<String> getSkills(String player, String urlFormat) {
		List<String> skills = new LinkedList<>();
		Resource resource;
		try {
			resource = new UrlResource(String.format(urlFormat, player));
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
			return new ArrayList<>();
		}
		try(BufferedReader skillReader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
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
			return new ArrayList<>();
		}
		
		return skills;
	}
}

@Slf4j
class GenerateDataUpdateFromDump extends TimerTask {

	private Router<BattleGroundEvent> routerRef;
	private DumpService dumpServiceRef;
	
	public GenerateDataUpdateFromDump(Router<BattleGroundEvent> routerRef, DumpService dumpServiceRef) {
		this.routerRef = routerRef;
		this.dumpServiceRef = dumpServiceRef;
	}
	
	@Override
	public void run() {
		log.debug("updating data from dump");
		Collection<BattleGroundEvent> events = this.dumpServiceRef.getUpdatesFromDumpService();
		events.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.routerRef.sendAllDataToQueues(events);
		return;
	}
	
}
