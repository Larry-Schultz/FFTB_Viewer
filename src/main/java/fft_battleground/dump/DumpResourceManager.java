package fft_battleground.dump;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import fft_battleground.event.model.ExpEvent;
import fft_battleground.model.BattleGroundTeam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpResourceManager {

	private static final String DUMP_HIGH_EXP_URL = "http://www.fftbattleground.com/fftbg/highexp.txt";
	private static final String DUMP_HIGH_LAST_ACTIVE_URL = "http://www.fftbattleground.com/fftbg/highdate.txt";
	private static final String DUMP_PLAYLIST_URL = "http://www.fftbattleground.com/fftbg/playlist.xml";
	private static final String DUMP_BOT_URL = "http://www.fftbattleground.com/fftbg/bots.txt";
	
	private static final String DUMP_PORTAIL_URL_FORMAT = "http://www.fftbattleground.com/fftbg/portrait/%s.txt";
	private static final String DUMP_ALLEGIANCE_URL_FORMAT = "http://www.fftbattleground.com/fftbg/allegiance/%s.txt";
	private static final String DUMP_USERSKILLS_URL_FORMAT = "http://www.fftbattleground.com/fftbg/userskills/%s.txt";
	private static final String DUMP_PRESTIGE_URL_FORMAT = "http://www.fftbattleground.com/fftbg/prestige/%s.txt";

	
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
		SimpleDateFormat dateFormatter = new SimpleDateFormat(DumpService.dateActiveFormatString);
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
		} catch(IOException e) {
			return null;
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
	
	@SneakyThrows
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
	
}
