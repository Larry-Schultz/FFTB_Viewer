package fft_battleground.bot.model.event;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.BetType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import lombok.Data;

@Data
public class BetEvent extends BattleGroundEvent {
	
	private static final BattleGroundEventType event = BattleGroundEventType.BET;

	private String player;
	private BattleGroundTeam team;
	private String betAmount;
	private String betText;
	private BetType betType;
	
	private PlayerRecord metadata;
	
	public BetEvent(String player, BattleGroundTeam team, String betAmount, String betText, BetType type) {
		super(event);
		this.player = player;
		this.betAmount = betAmount;
		this.team = team;
		this.betType = type;
		this.betText = betText;
	}
	
	public BetEvent(BetInfoEvent betInfoEvent) {
		super(event);
		this.player = betInfoEvent.getPlayer();
		this.betAmount = betInfoEvent.getBetAmount().toString();
		this.betText = betInfoEvent.generateBetString();
		this.betType = BetType.VALUE;
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
