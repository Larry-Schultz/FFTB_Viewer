package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.model.Gender;
import fft_battleground.repo.model.PlayerRecord;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FightEntryEvent extends BattleGroundEvent implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.FIGHT_ENTRY;

	private String player;
	private String className;
	private Gender gender;
	private String skill;
	private String exclusionSkill;
	
	private String command;
	
	private PlayerRecord metadata;
	private Integer gilCost;
	private Integer sortingGilCost;
	private boolean skillPrestige = false;
	private boolean exclusionSkillPrestige = false;
	
	private String classDescription;
	private String skillDescription;
	private String exclusionSkillDescription;
	
	private String classColor;
	private String skillColor;
	private String exclusionSkillColor;
	
	public FightEntryEvent() {
		super(type);
		this.gilCost = 0;
	}
	
	public FightEntryEvent(BattleGroundEventType type) {
		super(type);
		this.gilCost = 0;
	}

	public FightEntryEvent(String command, String player, String className, Gender gender, String skill, String exclusionSkill) {
		super(type);
		this.player = player;
		this.className = className;
		this.gender = gender;
		this.skill = skill;
		this.exclusionSkill = exclusionSkill;
		this.command = command;
		
		this.gilCost = 0;
	}
	
	public FightEntryEvent(BattleGroundEventType type, String command, String player) {
		super(type);
		this.player = player;
		this.command = command;
		
		this.gilCost = 0;
	}

}
