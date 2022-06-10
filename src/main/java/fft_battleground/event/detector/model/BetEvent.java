package fft_battleground.event.detector.model;

import fft_battleground.botland.bot.model.BetType;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class BetEvent extends BattleGroundEvent {
	
	private static final BattleGroundEventType event = BattleGroundEventType.BET;

	private String player;
	private BattleGroundTeam team;
	private String betAmount;
	private String betText;
	private BetType betType;
	private boolean allinbutFlag;
	private Boolean isSubscriber;
	
	private PlayerRecord metadata;
	
	public BetEvent(String player, BattleGroundTeam team, String betAmount, String betText, BetType type, Boolean isSubscriber) {
		super(event);
		this.player = player;
		this.betAmount = betAmount;
		this.team = team;
		this.betType = type;
		this.betText = betText;
		this.isSubscriber = isSubscriber;
	}
	
	public BetEvent(BetInfoEvent betInfoEvent) {
		super(event);
		this.player = betInfoEvent.getPlayer();
		this.betAmount = betInfoEvent.getBetAmount().toString();
		this.betText = betInfoEvent.generateBetString();
		this.betType = BetType.VALUE;
		this.team = betInfoEvent.getTeam();
		this.isSubscriber = betInfoEvent.getIsSubscriber();
	}

	public Integer getBetAmountInteger() {
		Integer value = Integer.valueOf(this.betAmount);
		return value;
	}

	@Override
	public String toString() {
		return "BetEvent [player=" + player + ", team=" + BattleGroundTeam.getTeamName(this.team) + ", betAmount=" + betAmount + "]";
	}

}
