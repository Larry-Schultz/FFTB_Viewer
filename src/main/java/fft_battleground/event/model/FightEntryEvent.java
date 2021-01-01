package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.model.Gender;
import fft_battleground.repo.model.PlayerRecord;

import lombok.Data;

@Data
public class FightEntryEvent extends BattleGroundEvent implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.FIGHT_ENTRY;

	private String player;
	private String className;
	private Gender gender;
	private String skill;
	private String exclusionSkill;
	
	private String command;
	
	private PlayerRecord metadata;
	
	public FightEntryEvent() {
		super(type);
	}

	public FightEntryEvent(String command, String player, String className, Gender gender, String skill, String exclusionSkill) {
		super(type);
		this.player = player;
		this.className = className;
		this.gender = gender;
		this.skill = skill;
		this.exclusionSkill = exclusionSkill;
	}

}
