package fft_battleground.event.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.event.GiftSkill;

import lombok.Data;

@Data
public class GiftSkillEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {

	private static final BattleGroundEventType type = BattleGroundEventType.GIFT_SKILL;
	private static final Integer cost = 2000;
	
	private List<GiftSkill> giftSkills;
	
	public GiftSkillEvent(String givingPlayer, String receivedSkill, String receivingPlayer) {
		super(type);
		this.giftSkills = new ArrayList<>();
		GiftSkill giftSkill = new GiftSkill(givingPlayer, new PlayerSkillEvent(receivingPlayer, receivedSkill));
		this.giftSkills = new ArrayList<>(Arrays.asList(new GiftSkill[] {giftSkill}));
	}
	
	public void addAdditionalGift(String givingPlayer, String receivedSkill, String receivingPlayer) {
		GiftSkill giftSkill = new GiftSkill(givingPlayer, new PlayerSkillEvent(receivingPlayer, receivedSkill));
		this.giftSkills.add(giftSkill);
	}
	
	public Integer getCost() {
		return cost;
	}
	
}
