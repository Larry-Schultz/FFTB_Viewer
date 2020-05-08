package fft_battleground.botland.model;

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
	PRESTIGE_ASCENSION("Prestige Ascension");
	
	BattleGroundEventType(String eventStringName) {
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
}
