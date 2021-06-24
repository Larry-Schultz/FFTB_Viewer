package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.util.SkillType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpDataProvider {

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
	private static final String DUMP_CLASSBONUS_URL_FORMAT ="http://www.fftbattleground.com/fftbg/classbonus/%s";
	private static final String DUMP_SKILLBONUS_URL_FORMAT = "http://www.fftbattleground.com/fftbg/skillbonus/%s";
	
	@Autowired
	private DumpResourceManager dumpResourceManager;
	
	@Autowired
	private DumpService dumpService;
	
	public Map<String, Integer> getHighScoreDump() throws DumpException {
		Map<String, Integer> data = new HashMap<>();
		
		Resource resource = this.dumpResourceManager.getUrlResource(DUMP_HIGH_SCORE_URL);
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
		
		Resource resource = this.dumpResourceManager.getUrlResource(DUMP_HIGH_SCORE_URL);
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
		Resource resource = this.dumpResourceManager.getUrlResource(DUMP_HIGH_EXP_URL);
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
		Resource resource = this.dumpResourceManager.getUrlResource(DUMP_HIGH_LAST_ACTIVE_URL);
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
	
	public Map<String, Integer> getSnubData() throws DumpException {
		Map<String, Integer> data = new HashMap<>();
		
		Resource resource = this.dumpResourceManager.getUrlResource(DUMP_SNUB_URL);
		try(BufferedReader highScoreReader = this.dumpResourceManager.openDumpResource(resource)) {
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
		portrait = this.readLineDataFromFile(player, DUMP_PORTRAIT_URL_FORMAT, "portrait", parseFunction);
		
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
		BattleGroundTeam allegiance = this.readLineDataFromFile(player, DUMP_ALLEGIANCE_URL_FORMAT, "allegiance", allegianceParseFunction);
		
		return allegiance;
	}
	
	public <T> T readLineDataFromFile(String player, String url, String dataType, Function<String, T> dataParseFunction) throws DumpException {
		T result;
		String playerName = StringUtils.lowerCase(player);
		Resource resource = this.dumpResourceManager.getUrlResource(this.getPlayerResourcePath(url, playerName));
		try(BufferedReader reader = this.dumpResourceManager.openDumpResource(resource)) {
			String line = reader.readLine();
			if(StringUtils.contains(line, "<!DOCTYPE")) {
				line = "";
			}
			result = dataParseFunction.apply(line);
		} catch (IOException e) {
			log.error("error getting {} for player {}", dataType, player, e);
			throw new DumpException(e);
		}
		return result;
		
	}
	
	public Set<String> getBots() throws DumpException {
		Set<String> bots = new HashSet<>();
		Resource resource = this.dumpResourceManager.getUrlResource(DUMP_BOT_URL);
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
	
	public List<String> getPrestigeSkillsForPlayer(String player) throws DumpException {
		List<PlayerSkills> skills = this.getSkills(player, DUMP_PRESTIGE_URL_FORMAT, SkillType.PRESTIGE);
		List<String> skillStrings = PlayerSkills.convertToListOfSkillStrings(skills); 
		return skillStrings;
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
	
	/**
	 * resuable function for reading data from a multi-line file
	 * @param player
	 * @param urlFormat
	 * @param collection
	 * @param parseFunction
	 * @throws DumpException
	 */
	public <T> void getDataFromMultilineFile(String player, String urlFormat, Collection<T> collection, Function<String, T> parseFunction) throws DumpException {
		Resource resource = this.dumpResourceManager.getUrlResource(this.getPlayerResourcePath(urlFormat, player));
		try(BufferedReader skillReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = skillReader.readLine()) != null) {
				T result = parseFunction.apply(line);
				if(result != null) {
					collection.add(result);
				}
			}
		} catch (IOException e) {
			log.debug("no user skills data for player {}", player);
			throw new DumpException(e);
		}
	}
	
	public String getMusicXmlString() throws DumpException {
		Resource resource = this.dumpResourceManager.getUrlResource(DUMP_PLAYLIST_URL);
		StringBuilder xmlData = new StringBuilder();
		String line;
		try(BufferedReader musicReader = this.dumpResourceManager.openDumpResource(resource)) {
			while((line = musicReader.readLine()) != null) {
				xmlData.append(line);
			}
		} catch (IOException e) {
			log.debug("could not access music data");
			throw new DumpException(e);
		}
		
		return xmlData.toString();
	}
	
	public Set<String> getPlayerList(String url) {
		String cleanedUrl = this.getPlayerListPath(url);
		Set<String> playerList = this.getPlayerListFromUrl(cleanedUrl);
		return playerList;
	}
	
	@SneakyThrows
	protected Set<String> getRecentPlayerListFromUrl(String url) {
		String cleanedUrl = this.getPlayerListPath(url);
		Set<String> playerList = this.dumpResourceManager.walkPlayerList(cleanedUrl);
		return playerList;
	}
	
	@SneakyThrows
	protected Set<String> getPlayerListFromUrl(String url) {
		Set<String> players = this.dumpResourceManager.getAllPlayersFromList(url);
		return players;
	}
	
	/**
	 * gets the full folder from a given format
	 * @param url
	 * @return
	 */
	protected String getPlayerListPath(String url) {
		String playerListPath = String.format(url, "");
		return playerListPath;
	}
	
	/**
	 * performs the format function on a given url format and player name
	 * @param url
	 * @param player
	 * @return
	 */
	protected String getPlayerResourcePath(String url, String player) {
		String playerResourcePath = String.format(url, player) + ".txt";
		return playerResourcePath;
	}


	
	protected String returnNullIfEmpty(String line) {
		String result = null;
		if(StringUtils.isNotBlank(line)) {
			result = line;
		}
		return result;
	}
	
}
