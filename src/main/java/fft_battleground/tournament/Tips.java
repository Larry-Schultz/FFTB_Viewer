package fft_battleground.tournament;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mjson.Json;

@Data
@Slf4j
public class Tips {
	private Map<String, String> item;
	private Date itemLastMod;
	private Map<String, String> ability;
	private Date abilityLastMod;
	private Map<String, String> userSkill;
	private Date userSkillLastMod;
	private Map<String, String> zodiac;
	private Date zodiacLastMod;
	private Map<String, String> classMap;
	private Date classLastMod;
	private Map<String, List<String>> monsterSkills;
	private Date monsterLastMod;
	
	public String getPlayerSkillMetadata(String skillName) {
		String metadata = null;
		if(metadata == null && this.item.containsKey(skillName)) {
			metadata = this.item.get(skillName);
		}
		if(metadata == null && this.ability.containsKey(skillName)) {
			metadata = this.ability.get(skillName);
		}
		if(metadata == null && this.userSkill.containsKey(skillName)) {
			metadata = this.userSkill.get(skillName);
		}
		
		if(metadata != null) {
			metadata = StringUtils.replace(metadata, "\"", "");
		}
		
		return metadata;
	}
	
	public Tips() {}
	
	public Tips(Resource tipsJsonResource) {
		this.item = new HashMap<>();
		this.ability = new HashMap<>();
		this.userSkill = new HashMap<>();
		this.zodiac = new HashMap<>();
		this.classMap = new HashMap<>();
		this.monsterSkills = new HashMap<>();
		
		this.parseJsonFile(tipsJsonResource);
	}
	
	@SneakyThrows
	protected void parseJsonFile(Resource tipsJsonResource) {
		String tipsData = "";
		
		Json tipsJson = Json.read(this.getJsonAsString(tipsJsonResource));
		//parse items
		this.item = this.parseJsonMapObject(tipsJson, "Item");
		this.itemLastMod = this.parseLastModified(tipsJson, "ItemLastMod");
		
		this.ability = this.parseJsonMapObject(tipsJson, "Ability");
		this.abilityLastMod = this.parseLastModified(tipsJson, "AbilityLastMod");
		
		this.userSkill = this.parseJsonMapObject(tipsJson, "UserSkill");
		this.userSkillLastMod = this.parseLastModified(tipsJson, "UserSkillLastMod");
		
		this.zodiac = this.parseJsonMapObject(tipsJson, "Zodiac");
		this.zodiacLastMod = this.parseLastModified(tipsJson, "ZodiacLastMod");
		
		this.classMap = this.parseJsonMapObject(tipsJson, "Class");
		this.classLastMod = this.parseLastModified(tipsJson, "ClassLastMod");
		
		this.monsterSkills = this.parseJsonMapListObject(tipsJson, "MonsterSkills");
		this.monsterLastMod = this.parseLastModified(tipsJson, "MonsterLastMod");
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
	
	protected Date parseLastModified(Json tipsJson, String parameter) throws ParseException {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		Date dateObj = sdf.parse(tipsJson.at(parameter).asString());
		return dateObj;
	}
	
	protected Map<String, List<String>> parseJsonMapListObject(Json tipsJson, String parameter) {
		Map<String, List<String>> normalMap = new HashMap<>();
		
		Map<String, Json> itemJsonMap = tipsJson.at(parameter).asJsonMap();
		for(String itemJsonMapKey : itemJsonMap.keySet()) {
			List<Json> jsonList = itemJsonMap.get(itemJsonMapKey).asJsonList();
			List<String> normalList = new ArrayList<>();
			for(int i = 0; i<jsonList.size(); i++) {
				normalList.add(jsonList.get(i).asString());
			}
			normalMap.put(itemJsonMapKey, normalList);
		}
		
		return normalMap;
	}
}
