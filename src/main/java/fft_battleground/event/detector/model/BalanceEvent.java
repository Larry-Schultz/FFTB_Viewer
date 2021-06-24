package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.repo.util.BalanceType;
import fft_battleground.repo.util.BalanceUpdateSource;
import lombok.Data;

@Data
public class BalanceEvent extends BattleGroundEvent {
	private static final BattleGroundEventType defaultType = BattleGroundEventType.OTHER_PLAYER_BALANCE;
	
	private String player;
	private Integer amount;
	private Integer spendable;
	private BalanceType balancetype;
	private BalanceUpdateSource balanceUpdateSource;
	
	private Integer balanceChange;
	
	public BalanceEvent(BattleGroundEventType event, Integer amount, Integer spendable) {
		super(event);
		this.amount = amount;
		this.spendable = spendable;
		this.balancetype = BalanceType.CHAT;
		this.balanceUpdateSource = BalanceUpdateSource.CHAT;
	}
	
	public BalanceEvent(BattleGroundEventType event, String username, Integer amount, Integer spendable) {
		super(event);
		this.player = username;
		this.amount = amount;
		this.spendable = spendable;
		this.balancetype = BalanceType.CHAT;
		this.balanceUpdateSource = BalanceUpdateSource.CHAT;
	}

	public BalanceEvent(String player, Integer amount, BalanceType balanceType, BalanceUpdateSource balanceUpdateSource) {
		super(defaultType);
		this.player = player;
		this.amount = amount;
		this.balancetype = balanceType;
		this.balanceUpdateSource = balanceUpdateSource;
	}

	@Override
	public String toString() {
		return "BalanceEvent [player=" + player + ", amount=" + amount + ", spendable=" + spendable + ", balancetype="
				+ balancetype + "]";
	}


}
