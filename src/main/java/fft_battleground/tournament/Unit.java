package fft_battleground.tournament;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.event.model.UnitInfoEvent;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Unit {
	@JsonProperty("Name")
	private String Name;
	@JsonProperty("Gender")
	private String Gender;
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
	
	public Unit() {}
	
	public UnitInfoEvent createUnitInfoEvent() {
		UnitInfoEvent event = new UnitInfoEvent(this.Name, this.getUnitInfoString(), this);
		return event;
	}
	
	public String getUnitInfoString() {
		String unitInfoString = "";
		
		if(!StringUtils.equals(this.Gender, "Monster")) {
			unitInfoString = this.createDashDelimitedString(Arrays.asList(new String[] {
				this.Name, this.Gender, this.Sign, this.Brave.toString(), this.Faith.toString(), this.className, this.ActionSkill,
				this.ReactionSkill, this.MoveSkill, this.Mainhand, this.Offhand, this.Head, this.Armor, this.Accessory,
				this.createCommaDelimitedString(this.ClassSkills), this.createCommaDelimitedString(this.ExtraSkills)
			}));
		} else {
			unitInfoString = this.createDashDelimitedString(Arrays.asList(new String[] {
					this.Name, this.Gender, this.Sign, this.Brave.toString(), this.Faith.toString(), this.className, this.ActionSkill,
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
