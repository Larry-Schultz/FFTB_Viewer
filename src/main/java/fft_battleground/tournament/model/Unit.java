package fft_battleground.tournament.model;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.model.Gender;
import fft_battleground.util.jackson.GenderDeserializer;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Unit {
	public static final List<String> itemsToPrefix = Arrays.asList(new String[] {"Bracer", "Elixir", "Kiyomori", "Soft", "X-Potion", "Remedy", "Muramasa",
			"Maiden's Kiss", "Masamune", "Murasame", "Eye Drop", "Shuriken", "Antidote", "Hi-Potion", "Heaven's Cloud", "Bizen Boat",
			"Hi-Ether", "Chirijiraden", "Kikuichimoji", "Potion", "Holy Water", "Spear", "Echo Grass", "Phoenix Down", "Ether"});
	public static final String itemSuffix = "-Item";
	
	@JsonProperty("Name")
	private String Name;
	
	@JsonDeserialize(using = GenderDeserializer.class)
	@JsonProperty("Gender")
	private Gender gender;
	
	@JsonProperty("Sign")
	private String Sign;
	
	@JsonProperty("Brave")
	private Short Brave;
	
	@JsonProperty("Faith")
	private Short Faith;
	
	@JsonProperty("Class")
	private String className;
	
	@JsonProperty("ActionSkill")
	private String ActionSkill;
	
	@JsonProperty("ReactionSkill")
	private String ReactionSkill;
	
	@JsonProperty("MoveSkill")
	private String MoveSkill;
	
	@JsonProperty("Mainhand")
	private String Mainhand;
	
	@JsonProperty("Offhand")
	private String Offhand;
	
	@JsonProperty("Head")
	private String Head;
	
	@JsonProperty("Armor")
	private String Armor;
	
	@JsonProperty("Accessory")
	private String Accessory;
	
	@JsonProperty("ClassSkills")
	private List<String> ClassSkills;
	
	@JsonProperty("ExtraSkills")
	private List<String> ExtraSkills;
	
	private boolean raidBoss = false;
	
	public Unit() {}
	
	public UnitInfoEvent createUnitInfoEvent() {
		UnitInfoEvent event = new UnitInfoEvent(this.Name, this.getUnitInfoString(), this);
		return event;
	}
	
	public String getUnitInfoString() {
		String unitInfoString = "";
		
		if(this.gender != Gender.MONSTER) {
			unitInfoString = this.createDashDelimitedString(Arrays.asList(new String[] {
				this.Name, this.gender.toString(), this.Sign, this.Brave.toString(), this.Faith.toString(), this.className, this.ActionSkill,
				this.ReactionSkill, this.MoveSkill, this.Mainhand, this.Offhand, this.Head, this.Armor, this.Accessory,
				this.createCommaDelimitedString(this.ClassSkills), this.createCommaDelimitedString(this.ExtraSkills)
			}));
		} else {
			unitInfoString = this.createDashDelimitedString(Arrays.asList(new String[] {
					this.Name, this.gender.toString(), this.Sign, this.Brave.toString(), this.Faith.toString(), this.className, this.ActionSkill,
					this.ReactionSkill
				}));
		}
		
		return unitInfoString;
	}
	
	public String createDashDelimitedString(List<String> strings) {
		String delimitedString = this.createDelimitedList(strings, " - ");
		return delimitedString;
	}
	
	public String createCommaDelimitedString(List<String> strings) {
		String delimitedString = this.createDelimitedList(strings, ", ");
		return delimitedString;
	}
	
	public String createDelimitedList(List<String> strings, String delimiter) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i<strings.size(); i++) {
			if(i != (strings.size() -1)) {
				builder.append(strings.get(i)).append(delimiter); 
			} else {
				builder.append(strings.get(i));
			}
		}
		
		return builder.toString();
	}
	
}
