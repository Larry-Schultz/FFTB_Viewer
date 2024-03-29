package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Unit;
import fft_battleground.tournament.tips.UnitStats;
import fft_battleground.tournament.tips.UnitTipInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class UnitInfoEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.UNIT_INFO;
	
	private String player;
	private String unitInfoString;
	private Unit unit;
	private Boolean isRaidBoss = false;
	private BattleGroundTeam team;
	private Integer position;
	
	private UnitTipInfo unitTipInfo;
	private UnitStats unitStats;

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
		return "UnitInfoEvent [player=" + player + ", unitInfoString=" + unitInfoString + ", unit=" + unit
				+ ", isRaidBoss=" + isRaidBoss + "]";
	}
	
	
}
