package fft_battleground.bot.model.event;

import java.util.Arrays;
import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.DatabaseResultsData;
import lombok.Data;

@Data
public class GiftSkillEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {

	private static final BattleGroundEventType type = BattleGroundEventType.GIFT_SKILL;
	private static final Integer cost = 5000;
	
	public String givingPlayer;
	public PlayerSkillEvent playerSkillEvent;
	
	public GiftSkillEvent(String givingPlayer, String receivedSkill, String receivingPlayer) {
		super(type);
		this.givingPlayer = givingPlayer;
		this.playerSkillEvent = new PlayerSkillEvent(receivingPlayer, Arrays.asList(new String[] {receivedSkill}));
	}
	
	public Integer getCost() {
		return cost;
	}
	
	@Override
	public String toString() {
		return "GiftSkillEvent [player=" + givingPlayer + ", buySkillEvent=" + playerSkillEvent + "]";
	}
}
