package fft_battleground.dump.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import fft_battleground.dump.model.FolderListData;
import fft_battleground.exception.DumpException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDataProvider {

	protected DumpResourceManager dumpResourceManager;
	
	public AbstractDataProvider(DumpResourceManager dumpResourceManager) {
		this.dumpResourceManager = dumpResourceManager;
	}
	
	public <T> T readSingleLineDataFromFile(String player, String url, String dataType, Function<String, T> dataParseFunction) throws DumpException {
		T result;
		String playerName = StringUtils.lowerCase(player);
		Resource resource = this.getUrlResource(this.getPlayerResourcePath(url, playerName));
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
	
	/**
	 * resuable function for reading data from a multi-line file
	 * @param player
	 * @param urlFormat
	 * @param collection
	 * @param parseFunction
	 * @throws DumpException
	 */
	public <T> void getDataFromMultilineFile(String player, String urlFormat, Collection<T> collection, Function<String, T> parseFunction) throws DumpException {
		String url = this.getPlayerResourcePath(urlFormat, player);
		try {
			this.getDataFromMultilineFile(url, collection, parseFunction);
		} catch(DumpException e) {
			log.debug("no user skills data for player {}", player);
			throw e;
		}
	}
	
	public <T> void getDataFromMultilineFile(String url, Collection<T> collection, Function<String, T> parseFunction) throws DumpException {
		try(BufferedReader skillReader = this.getReaderForUrl(url)) {
			String line;
			while((line = skillReader.readLine()) != null) {
				T result = parseFunction.apply(line);
				if(result != null) {
					collection.add(result);
				}
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
	}
	
	public Set<String> getPlayerList(String url) {
		String cleanedUrl = this.getPlayerListPath(url);
		Set<String> playerList = this.getPlayerListFromUrl(cleanedUrl);
		return playerList;
	}
	
	@SneakyThrows
	protected Set<String> getRecentPlayerListFromUrl(String url) {
		String cleanedUrl = this.getPlayerListPath(url);
		Set<String> playerList = this.walkPlayerList(cleanedUrl);
		return playerList;
	}
	
	@SneakyThrows
	protected Set<String> getPlayerListFromUrl(String url) {
		Set<String> players = this.getAllPlayersFromList(url);
		return players;
	}
	
	protected BufferedReader getReaderForUrl(String url) throws IOException, DumpException {
		Resource resource = this.getUrlResource(url);
		BufferedReader reader = this.dumpResourceManager.openDumpResource(resource);
		return reader;
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
	
	protected Set<String> getAllPlayersFromList(String url) throws DumpException {
		Set<FolderListData> playerList = this.getPlayerDataFromList(url);
		Set<String> players = playerList.parallelStream().map(playerListData -> playerListData.getEntityName()).collect(Collectors.toSet());
		return players;
	}
	
	protected Set<String> walkPlayerList(String url) throws DumpException {
		Pair<Integer, Integer> initialTimeUnitTimeLengthPair = new ImmutablePair<>(Calendar.HOUR, 1);
		Pair<Integer, Integer> walkTimeUnitTimeLengthPair = new ImmutablePair<>(Calendar.MINUTE, 10);
		Set<String> players = this.getRecentlyUpdatedPlayers(url, initialTimeUnitTimeLengthPair, walkTimeUnitTimeLengthPair);
		return players;
	}
	
	/**
	 * Finds the latest time an updated to this file was performed, and get the data for then entries that match that update.
	 * @param url
	 * @param initialTimeUnitTimeLengthPair
	 * @param walkTimeUnitTimeLengthPair
	 * @return
	 * @throws DumpException
	 */
	protected Set<String> getRecentlyUpdatedPlayers(String url, Pair<Integer, Integer> initialTimeUnitTimeLengthPair, Pair<Integer, Integer> walkTimeUnitTimeLengthPair) throws DumpException {
		List<FolderListData> entriesWithNullLastUpdated = new ArrayList<>();
		List<FolderListData> playerList = this.getPlayerDataFromList(url).stream()
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
		
		List<FolderListData> recentPlayers = new ArrayList<>();
		FolderListData previous = null;
		for(FolderListData playerData: playerList) {
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
		
		Set<String> players = recentPlayers.parallelStream().map(playerListData -> playerListData.getEntityName()).collect(Collectors.toSet());
		return players;
	}
	
	protected Set<FolderListData> getPlayerDataFromList(String url) throws DumpException {
		Set<FolderListData> playerListData = this.getFolderDataFromList(url, "txt");
		return playerListData;
	}
	
	protected Set<FolderListData> getFolderDataFromList(String url, String entityFileExtension) throws DumpException {
		Set<FolderListData> playerList = new HashSet<>();
		Document doc = this.dumpResourceManager.openPlayerList(url);
		
		Elements playerNodes = doc.select("a[href$=" + entityFileExtension + "]");
		for(Element element : playerNodes) {
			String player = null;
			Date lastUpdatedDate = null;
			String filename = element.attr("href");
			if(StringUtils.isNotBlank(filename)) {
				player = StringUtils.replace(filename, "." + entityFileExtension, "");
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
			FolderListData data = new FolderListData(player, lastUpdatedDate);
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
	protected Resource getUrlResource(String path) throws DumpException {
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
