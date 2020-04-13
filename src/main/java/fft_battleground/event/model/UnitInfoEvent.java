package fft_battleground.event.model;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.tournament.Unit;
import lombok.Data;

@Data
public class UnitInfoEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.UNIT_INFO;
	
	private String player;
	private String unitInfoString;
	private Unit unit;

	public UnitInfoEvent() {}
	
	public UnitInfoEvent(String username, String unitInfoString) {
		super(type);
		this.player = username;
		this.unitInfoString = unitInfoString;
	}

	public UnitInfoEvent(String name, String unitInfoString, Unit unit) {
		super(type);
		this.player = name;
		this.unitInfoString = unitInfoString;
		this.unit = unit;
	}

	@Override
	public String toString() {
		return "UnitInfoEvent [username=" + player + ", unitInfoString=" + unitInfoString + "]";
	}
	
	
}
