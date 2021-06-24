package fft_battleground.event.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BattleGroundEventType {
	FIGHT_BEGINS("Fight Begins"),
	BETTING_BEGINS("Betting Begins"),
	BETTING_ENDS("Betting Ends"),
	BATTLE_ENDS("Battle Ends"),
	BALANCE("Balance"),
	OTHER_PLAYER_BALANCE("Other Player Balance"),
	LEVEL_UP("Level Up"),
	BET("Bet"),
	WIN("Win"),
	LOSS("Loss"),
	MATCH_INFO("Match"),
	TEAM_INFO("Team"), 
	EXP("Exp"),
	OTHER_PLAYER_EXP("Other Player Exp"),
	UNIT_INFO("Unit"), 
	BAD_BET("Bad Bet"), 
	ALLEGIANCE("Allegiance"), 
	SKILL_DROP("Skill Drop"), 
	PLAYER_SKILL("Player Skill"), 
	BUY_SKILL("Buy Skill"), 
	RESULT("Result"), 
	PORTRAIT("Portrait"),
	SKILL_WIN("Skill Win"), 
	BET_INFO("Bet Info"), 
	PRESTIGE_SKILLS("Prestige Player Skills"), 
	LAST_ACTIVE("Last Active"), 
	GIFT_SKILL("Gift Skill"), 
	GLOBAL_GIL_COUNT_UPDATE("Global Gil Count Update"), 
	PRESTIGE_ASCENSION("Prestige Ascension"), 
	MUSIC("Music"), 
	RISER_SKILL_WIN("Riser Skill Win"), 
	TOURNAMENT_STATUS_UPDATE_EVENT("Tournament Status Update"), 
	FIGHT_ENTRY("Fight entry"), 
	INVALID_FIGHT_ENTRY_COMBINATION("Invalid fight syntax"), 
	INVALID_FIGHT_ENTRY_CLASS("Invalid fight class or monster"), 
	UNOWNED_SKILL("Unowned skill"),
	SKILL_ON_COOLDOWN("Skill On Cooldown"),
	OTHER_PLAYER_UNOWNED_SKILL("Composite Unowned Skill Event"), 
	OTHER_PLAYER_INVALID_FIGHT_COMBINATION("Composite Invalid Fight Combination"), 
	OTHER_PLAYER_INVALID_FIGHT_CLASS("Compositive Invalid Fight Class"), 
	OTHER_PLAYER_SKILL_ON_COOLDOWN("Composite Skill On Cooldown"),
	DONT_FIGHT("Don't Fight"), 
	BUY_SKILL_RANDOM("Buy Skill Random"), 
	CLASS_BONUS("Class bonus"), 
	SKILL_BONUS("Skill bonus"), 
	SNUB("Failed attempts to join !fight"), 
	OTHER_PLAYER_SNUB("snub event list");
	
	private BattleGroundEventType(String eventStringName) {
		this.eventStringName = eventStringName;
	}
	
	private String eventStringName;
	
	public String getEventStringName() {
		return this.eventStringName;
	}
	
	@Override
	public String toString() {
		return this.eventStringName;
	}
	
	@JsonValue
	public String jsonValue() {
		String result = StringUtils.upperCase(this.name());
		return result;
	}
}
