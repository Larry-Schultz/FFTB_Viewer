package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.dump.cache.map.leaderboard.ExpLeaderboardByRank;
import fft_battleground.dump.cache.map.leaderboard.ExpRankLeaderboardByPlayer;
import fft_battleground.dump.cache.map.leaderboard.PlayerLeaderboardCache;
import fft_battleground.dump.data.AbstractDataProvider;
import fft_battleground.dump.data.DumpResourceManager;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.skill.model.SkillType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpDataProvider extends AbstractDataProvider {
	public static final String dateActiveFormatString = "EEE MMM dd HH:mm:ss z yyyy";
	
	private static final String DUMP_HIGH_SCORE_URL = "http://www.fftbattleground.com/fftbg/highscores.txt";
	private static final String DUMP_HIGH_EXP_URL = "http://www.fftbattleground.com/fftbg/highexp.txt";
	private static final String DUMP_HIGH_LAST_ACTIVE_URL = "http://www.fftbattleground.com/fftbg/highdate.txt";
	private static final String DUMP_SNUB_URL = "http://www.fftbattleground.com/fftbg/highsnub.txt";
	private static final String DUMP_BOT_URL = "http://www.fftbattleground.com/fftbg/bots.txt";
	private static final String DUMP_PLAYLIST_URL = "http://www.fftbattleground.com/fftbg/playlist.xml";
	
	private static final String DUMP_PORTRAIT_URL_FORMAT = "http://www.fftbattleground.com/fftbg/portrait/%s";
	private static final String DUMP_ALLEGIANCE_URL_FORMAT = "http://www.fftbattleground.com/fftbg/allegiance/%s";
	private static final String DUMP_USERSKILLS_URL_FORMAT = "http://www.fftbattleground.com/fftbg/userskills/%s";
	private static final String DUMP_PRESTIGE_URL_FORMAT = "http://www.fftbattleground.com/fftbg/prestige/%s";
	private static final String DUMP_PRESTIGE_COOLDOWN_URL_FORMAT = "http://www.fftbattleground.com/fftbg/prestige/snubstreak/%s";
	private static final String DUMP_CLASSBONUS_URL_FORMAT ="http://www.fftbattleground.com/fftbg/classbonus/%s";
	private static final String DUMP_SKILLBONUS_URL_FORMAT = "http://www.fftbattleground.com/fftbg/skillbonus/%s";

	@Autowired
	private PlayerLeaderboardCache playerLeaderboardCache;
	
	@Autowired
	private ExpLeaderboardByRank expLeaderboardByRank;
	
	@Autowired
	private ExpRankLeaderboardByPlayer expRankLeaderboardByPlayer;
	
	public DumpDataProvider(@Autowired DumpResourceManager dumpResourceManager) {
		super(dumpResourceManager);
	}
	
	public Map<String, Integer> getHighScoreDump() throws DumpException {
		Map<String, Integer> data = new HashMap<>();
		
		try(BufferedReader highScoreReader= this.getReaderForUrl(DUMP_HIGH_SCORE_URL)) {
			String line;
			highScoreReader.readLine(); //ignore the header
			
			while((line = highScoreReader.readLine()) != null) {
				Integer position = Integer.valueOf(StringUtils.substringBefore(line, ". "));
				String username = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":")));
				Integer value = Integer.valueOf(StringUtils.replace(StringUtils.substringBetween(line, ": ", "G"), ",", ""));
				data.put(username, value);
				this.playerLeaderboardCache.put(username, position);
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
		
		try(BufferedReader highScoreReader = this.getReaderForUrl(DUMP_HIGH_SCORE_URL)) {
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
		try(BufferedReader highScoreReader = this.getReaderForUrl(DUMP_HIGH_EXP_URL)) {
			String line;
			highScoreReader.readLine(); //ignore the header
			while((line = highScoreReader.readLine()) != null) {
				Integer position = Integer.valueOf(StringUtils.substringBefore(line, "."));
				String username = StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":"));
				Short level = Short.valueOf(StringUtils.substringBetween(StringUtils.replace(line, ",", ""), "Level ", " (EXP:"));
				Short currentExp = Short.valueOf(StringUtils.substringBetween(line, "(EXP: ", ")"));
				Short remainingExp = (short) (100 - currentExp);
				data.put(username, new ExpEvent(username, level, remainingExp));
				this.expLeaderboardByRank.put(position, username);
				this.expRankLeaderboardByPlayer.put(username, position);
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
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateActiveFormatString);
		try(BufferedReader highDateReader = this.getReaderForUrl(DUMP_HIGH_LAST_ACTIVE_URL)) {
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
	
	public Map<String, Integer> getSnubData() throws DumpException {
		Map<String, Integer> data = new HashMap<>();
		
		try(BufferedReader highScoreReader = this.getReaderForUrl(DUMP_SNUB_URL)) {
			String line;
			highScoreReader.readLine(); //ignore the header
			
			while((line = highScoreReader.readLine()) != null) {
				Integer position = Integer.valueOf(StringUtils.substringBefore(line, ". "));
				String username = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBetween(line, ". ", ":")));
				Integer tries = Integer.valueOf(StringUtils.trim(StringUtils.substringBetween(line, ": ", " tries")));
				data.put(username, tries);
			}
		} catch (DumpException e) {
			log.error("Error getting high snub dump");
			throw e;
		} catch (IOException e) {
			log.error("Error getting high snub dump");
			throw new DumpException(e);
		}
		
		return data;
	}
	
	public Set<String> getPlayersForPortraitDump() {
		Set<String> players = this.getPlayerList(DUMP_PORTRAIT_URL_FORMAT);
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
	
	public Set<String> getRecentPlayersForPrestigeSkillsDump() {
		Set<String> players = this.getRecentPlayerListFromUrl(DUMP_PRESTIGE_URL_FORMAT);
		return players;
	}
	
	public Set<String> getPlayersForPrestigeSkillsCooldown() {
		Set<String> player = this.getPlayerList(DUMP_PRESTIGE_COOLDOWN_URL_FORMAT);
		return player;
	}
	
	public Set<String> getRecentPlayersForPrestigeSkillsCooldown() {
		Set<String> player = this.getRecentPlayerListFromUrl(DUMP_PRESTIGE_COOLDOWN_URL_FORMAT);
		return player;
	}
	
	public Set<String> getPlayersForUserSkillsDump() {
		Set<String> players = this.getPlayerList(DUMP_USERSKILLS_URL_FORMAT);
		return players;
	}
	
	@SneakyThrows
	public Set<String> getRecentPlayersForUserSkillsDump() {
		Set<String> players = this.getRecentPlayerListFromUrl(DUMP_USERSKILLS_URL_FORMAT);
		return players;
	}
	
	public Set<String> getPlayersForClassBonusDump() {
		Set<String> players = this.getPlayerList(DUMP_CLASSBONUS_URL_FORMAT);
		return players;
	}
	
	@SneakyThrows
	public Set<String> getRecentPlayersForClassBonusDump() {
		Set<String> players = this.getRecentPlayerListFromUrl(DUMP_CLASSBONUS_URL_FORMAT);
		return players;
	}
	
	public Set<String> getPlayersForSkillBonusDump() {
		Set<String> players = this.getPlayerList(DUMP_SKILLBONUS_URL_FORMAT);
		return players;
	}
	
	@SneakyThrows
	public Set<String> getRecentPlayersForSkillBonusDump() {
		Set<String> players = this.getRecentPlayerListFromUrl(DUMP_SKILLBONUS_URL_FORMAT);
		return players;
	}
	
	public String getPortraitForPlayer(String player) throws DumpException {
		String portrait;
		Function<String, String> parseFunction = this::returnNullIfEmpty;
		portrait = this.readSingleLineDataFromFile(player, DUMP_PORTRAIT_URL_FORMAT, "portrait", parseFunction);
		
		return portrait;
	}
	
	public BattleGroundTeam getAllegianceForPlayer(String player) throws DumpException {
		Function<String, BattleGroundTeam> allegianceParseFunction = allegianceStr -> {
			BattleGroundTeam allegiance = null;
			if(StringUtils.isNotBlank(allegianceStr) && NumberUtils.isCreatable(allegianceStr)) {
				allegiance = BattleGroundTeam.parse(Integer.valueOf(allegianceStr)); 
			} else {
				log.debug("non numeric allegiance found, for player {} with data {}", player, allegianceStr);
				allegiance = BattleGroundTeam.NONE;
			}
			return allegiance;
		};
		BattleGroundTeam allegiance = this.readSingleLineDataFromFile(player, DUMP_ALLEGIANCE_URL_FORMAT, "allegiance", allegianceParseFunction);
		
		return allegiance;
	}
	
	public Set<String> getBots() throws DumpException {
		Set<String> bots = new HashSet<>();
		try(BufferedReader botReader = this.getReaderForUrl(DUMP_BOT_URL)) {
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
	
	public Set<String> getClassBonus(String player) throws DumpException {
		Set<String> classBonuses = new HashSet<>();
		Function<String, String> parseFunction = this::returnNullIfEmpty;
		this.getDataFromMultilineFile(player, DUMP_CLASSBONUS_URL_FORMAT, classBonuses, parseFunction);
		
		return classBonuses;
	}
	
	public Set<String> getSkillBonus(String player) throws DumpException {
		Set<String> skillBonuses = new HashSet<>();
		Function<String, String> parseFunction = this::returnNullIfEmpty;
		this.getDataFromMultilineFile(player, DUMP_SKILLBONUS_URL_FORMAT, skillBonuses, parseFunction);
		
		return skillBonuses;
	}
	
	public List<PlayerSkills> getSkillsForPlayer(String player) throws DumpException {
		List<PlayerSkills> skills = this.getSkills(player, DUMP_USERSKILLS_URL_FORMAT, SkillType.USER);
		return skills;
	}
	
	public List<PrestigeSkills> getPrestigeSkillsForPlayer(String player) throws DumpException {
		List<PrestigeSkills> skills = this.getSkills(player, DUMP_PRESTIGE_URL_FORMAT, SkillType.PRESTIGE).stream()
										.map(PrestigeSkills::new).collect(Collectors.toList());
		List<Integer> cooldowns = this.getPrestigeSkillCooldownsForPlayer(player, skills.size());
		for(int i = 0; i < skills.size(); i++) {
			skills.get(i).setCooldown(cooldowns.get(i));
		}
		
		return skills;
	}
	
	public List<Integer> getPrestigeSkillCooldownsForPlayer(String player, int skillCount) throws DumpException {
		List<Integer> cooldownList = new ArrayList<>();
		for(int i = 1; i <= skillCount; i++) {
			String alteredPlayer = player + "%20" + String.valueOf(i);
			Function<String, Integer> stringToInteger = Integer::valueOf;
			int cooldown = this.readSingleLineDataFromFile(alteredPlayer, DUMP_PRESTIGE_COOLDOWN_URL_FORMAT, "prestige cooldown", stringToInteger);
			cooldownList.add(cooldown);
		}
		
		return cooldownList;
	}
	
	protected List<PlayerSkills> getSkills(String player, String urlFormat, SkillType type) throws DumpException {
		List<PlayerSkills> skills = new LinkedList<>();
		Function<String, PlayerSkills> parseFunction = line -> {
			PlayerSkills playerSkill = null;
			String skill = null;
			int cooldown = 0;
			if(line.length() < 50 && !StringUtils.contains(line, "<")) {
				//remove the '~' characters
				skill = StringUtils.replace(line, "~", "");
				cooldown = StringUtils.countMatches(line, "~");
			}
			playerSkill = new PlayerSkills(skill, cooldown, type);
			return playerSkill;
		};
		this.getDataFromMultilineFile(player, urlFormat, skills, parseFunction);
		
		return skills;
	}
	
	public String getMusicXmlString() throws DumpException {
		StringBuilder xmlData = new StringBuilder();
		String line;
		try(BufferedReader musicReader = this.getReaderForUrl(DUMP_PLAYLIST_URL)) {
			while((line = musicReader.readLine()) != null) {
				xmlData.append(line);
			}
		} catch (IOException e) {
			log.debug("could not access music data");
			throw new DumpException(e);
		}
		
		return xmlData.toString();
	}
	
}
