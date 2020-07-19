package fft_battleground.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import fft_battleground.repo.model.TeamInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import mjson.Json;

@Data
@Slf4j
public class Images {

	private Map<String, String> characters;
	
	private List<Portrait> potraits;
	private String portraitsBaseUrl;
	
	public Images() {}
	
	@SneakyThrows
	public Images(Resource jsonFileLocation, String portraitFolderLocation, String browserBaseUrl) {
		
		Json tipsJson = Json.read(this.getJsonAsString(jsonFileLocation));
		this.characters = this.parseJsonMapObject(tipsJson, "Characters");
		this.potraits = this.parsePotraitFolder(portraitFolderLocation);
		this.portraitsBaseUrl = browserBaseUrl;
	}
	
	@SneakyThrows
	protected String getJsonAsString(Resource jsonFileLocation) {
		StringBuilder jsonBuilder = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(jsonFileLocation.getInputStream()))) {
			String line = reader.readLine();
			while(line != null) {
				jsonBuilder.append(line);
				line = reader.readLine();
			}
		}
		
		return jsonBuilder.toString();
	}
	
	protected Map<String, String> parseJsonMapObject(Json tipsJson, String parameter) {
		Map<String, String> normalMap = new HashMap<String, String>();
		
		Map<String, Json> itemJsonMap = tipsJson.at(parameter).asJsonMap();
		for(String itemJsonMapKey : itemJsonMap.keySet()) {
			normalMap.put(itemJsonMapKey, itemJsonMap.get(itemJsonMapKey).toString());
		}
		
		return normalMap;
	}
	
	@SneakyThrows
	protected List<Portrait> parsePotraitFolder(String location) {
		List<Portrait> results = null;
		try {
			results = this.parsePotraitFolderTraditionally(location);
		} catch(FileSystemNotFoundException e) {
			results = this.parsePotraitFolderUsingFileSystem(location);
		}
		
		return results;
	}
	
	@SneakyThrows
	protected List<Portrait> parsePotraitFolderTraditionally(String location) {
		List<Portrait> portraits = new ArrayList<>();
		
		Path locationPath = Paths.get(new ClassPathResource(location).getURI());
		try (Stream<Path> paths = Files.walk(locationPath)) {
			portraits = paths.map(path -> new Portrait(path)).collect(Collectors.toList());
	    } catch (IOException e) {
	      log.error("Error parsing potraits folder file names", e);
	    }
		
		return portraits;
	}
	
	@SneakyThrows
	protected List<Portrait> parsePotraitFolderUsingFileSystem(String location) {
		List<Portrait> portraits = new ArrayList<>();
		log.info("the portrait location is: {}", location);
		URI uri = getClass().getResource(location).toURI();
		final Map<String, String> env = new HashMap<>();
		final String[] array = uri.toString().split("!");
		final FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), env);
		final Path locationPath = fs.getPath(array[1]);
		//Path locationPath = Paths.get(path);
		try (Stream<Path> paths = Files.walk(locationPath)) {
			portraits = paths.map(path -> new Portrait(path)).collect(Collectors.toList());
	    } catch (IOException e) {
	      log.error("Error parsing potraits folder file names", e);
	    }
		
		return portraits;
	}
	
	public String getCharacterImagePath(String character) {
		String characterCapitalized = StringUtils.capitalize(character);
		String imagePath = this.characters.get(characterCapitalized);
		
		if(imagePath != null) {
			imagePath = StringUtils.remove(imagePath, "\"");
		}
		
		return imagePath;
	}
	
	public String getPortraitByName(String name) {
		String result = null;
		
		String nameCapitalized = StringUtils.capitalize(name);
		Optional<Portrait> possibleMatch = this.potraits.parallelStream().filter(portraits -> StringUtils.equalsIgnoreCase(nameCapitalized , StringUtils.replace(portraits.getLocation(), ".gif", ""))).findFirst();

		if(possibleMatch.isPresent()) {
			result = this.portraitsBaseUrl + possibleMatch.get().getLocation();
		} else {
			result = null;
		}
		
		return result;
	}
	
	public String getPortraitByName(String name, BattleGroundTeam allegiance) {
		String result = null;
		if(allegiance == null || allegiance == BattleGroundTeam.NONE) {
			result = this.getPortraitByName(name);
		} else {
			result = this.getPortraitByNameAndAllegiance(name, allegiance);
		}
		
		return result;
	}
	
	protected String getPortraitByNameAndAllegiance(String name, BattleGroundTeam allegiance) {
		String result = null;
		
		String nameCapitalized = StringUtils.capitalize(name);
		List<Portrait> possibleMatches = this.potraits.parallelStream().filter(portraits -> StringUtils.startsWithIgnoreCase(StringUtils.replace(portraits.getLocation(), ".gif", ""), nameCapitalized)).collect(Collectors.toList());
		
		//if only one result, its either a monster, a hero or a portrait I don't have
		if(possibleMatches.size() == 0) {
			result = null;
		} else if(possibleMatches.size() == 1) {
			result = this.portraitsBaseUrl + possibleMatches.get(0).getLocation();
		} else {
			Optional<Portrait> maybeMoreSpecificMatch = possibleMatches.parallelStream().filter(portraits -> portraits.getColor() == allegiance).findFirst();
			if(maybeMoreSpecificMatch.isPresent()) {
				result = this.portraitsBaseUrl + maybeMoreSpecificMatch.get().getLocation();
			} else {
				result = null;
			}
		}
		
		return result;
	}
	
	public String getPortraitLocationByTeamInfo(TeamInfo teamInfo) {
		String result = null;
		Optional<Portrait> possibleMatch = null;
		if(StringUtils.equalsIgnoreCase(teamInfo.getGender(), "Monster")) {
			possibleMatch = this.potraits.stream().filter(portraits -> StringUtils.equalsIgnoreCase(teamInfo.getClassName(), portraits.getClassName())).findFirst();
		} else {
			possibleMatch = this.potraits.stream().
					filter(portraits -> StringUtils.equalsIgnoreCase(teamInfo.getClassName(), portraits.getClassName()) && StringUtils.equalsIgnoreCase(teamInfo.getGender(), portraits.getGender())
							&& teamInfo.getTeam() == portraits.getColor()).findFirst();
		}
		
		if(possibleMatch.isPresent()) {
			result = this.portraitsBaseUrl + possibleMatch.get().getLocation();
		} else {
			result = null;
		}
		
		return result;
	}
}

@Data
@AllArgsConstructor
class Portrait {
	private String className;
	private String gender;
	private BattleGroundTeam color;
	private String location;
	
	public Portrait() {}
	
	public Portrait(Path path) {
		String fileName = path.getFileName().toString();
		if(StringUtils.contains(fileName, "M_")) {
			this.className = StringUtils.substringBefore(fileName, "M_");
			this.gender = "Male";
			this.location = fileName;
			this.color = this.getColor(fileName);
		} else if(StringUtils.contains(fileName, "M.")) {
			this.className = StringUtils.substringBefore(fileName, "M.");
			this.gender = "Male";
			this.location = fileName;
			this.color = BattleGroundTeam.NONE;
		} else if(StringUtils.contains(fileName, "F_")) {
			this.className = StringUtils.substringBefore(fileName, "F_");
			this.gender = "Female";
			this.color = this.getColor(fileName);
		} else if(StringUtils.contains(fileName, "F.")) {
			this.className = StringUtils.substringBefore(fileName, "F.");
			this.gender = "Female";
			this.color = BattleGroundTeam.NONE;
		} else {
			this.className = StringUtils.substringBefore(fileName, ".");
			this.gender = "Monster";
			this.color = BattleGroundTeam.NONE;
		}
		
		this.location = fileName;
	}
	
	public boolean isMonster() {
		boolean result = StringUtils.equalsIgnoreCase(gender, "Monster");
		return result;
	}
	
	protected BattleGroundTeam getColor(String str) {
		String substring = StringUtils.substringBetween(str, "_", ".");
		BattleGroundTeam team = BattleGroundTeam.parse(substring);
		if(team == null) {
			team = BattleGroundTeam.NONE;
		}
		
		return team;
	}
}
