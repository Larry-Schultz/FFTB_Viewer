package fft_battleground.event.model;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import lombok.Data;

@Data
public class BetInfoEvent extends BattleGroundEvent {
	private static final BattleGroundEventType event = BattleGroundEventType.BET_INFO;
	
	private String player;
	private Integer betAmount;
	private BattleGroundTeam team;
	private String percentage;
	private Integer possibleEarnings;
	
	private PlayerRecord metadata;

	public BetInfoEvent() {}
	
	public BetInfoEvent(String player, Integer betAmount, BattleGroundTeam team, String percentage, Integer possibleEarnings) {
		super(event);
		this.player = player;
		this.betAmount = betAmount;
		this.team = team;
		this.percentage = percentage;
		this.possibleEarnings = possibleEarnings;
	}
	
	public String generateBetString() {
		String betString = "!bet " + StringUtils.lowerCase(this.getTeam().toString()) + " " + this.getBetAmount();
		return betString;
	}


	@Override
	public String toString() {
		return "BetInfoEvent [player=" + player + ", betAmount=" + betAmount + ", team=" + team + ", percentage="
				+ percentage + ", possibleEarnings=" + possibleEarnings + "]";
	}
	



}
