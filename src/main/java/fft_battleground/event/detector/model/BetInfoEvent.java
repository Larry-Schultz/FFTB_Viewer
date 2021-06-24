package fft_battleground.event.detector.model;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BattleGroundEventType;
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
	private Boolean isSubscriber;
	
	private PlayerRecord metadata;

	public BetInfoEvent() {}
	
	public BetInfoEvent(String player, Integer betAmount, BattleGroundTeam team, String percentage, Integer possibleEarnings) {
		super(event);
		this.player = player;
		this.betAmount = betAmount;
		this.team = team;
		this.percentage = percentage;
		this.possibleEarnings = possibleEarnings;
		
		this.isSubscriber = null; //must be null specifically so we can inherit subscriber status
	}

	public BetInfoEvent(String player, Integer betAmount, BattleGroundTeam currentTeam) {
		super(event);
		this.player = player;
		this.betAmount = betAmount;
		this.team = currentTeam;
		
		this.percentage = null;
		this.possibleEarnings = null;
		this.isSubscriber = null;
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
