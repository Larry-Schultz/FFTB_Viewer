package fft_battleground.tournament.tips;

import java.util.HashSet;
import java.util.Set;

import fft_battleground.mustadio.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitStats implements Stats, Cloneable {
	private int hp = 0;
	private int mp = 0;
	private int move = 0;
	private int jump = 0;
	private int speed = 0;
	private int pa = 0;
	private int ma = 0;
	private int cEv = 0;
	private Set<Status> permStatuses = new HashSet<>();
	private Set<Status> initialStatuses = new HashSet<>();
	
	public void addStats(Stats addStats) {
		this.hp = this.hp + addStats.getHp();
		this.mp = this.mp + addStats.getMp();
		this.move = this.move + addStats.getMove();
		this.jump = this.jump + addStats.getJump();
		this.speed = this.speed + addStats.getSpeed();
		this.pa = this.pa + addStats.getPa();
		this.ma = this.ma + addStats.getMa();
		this.permStatuses.addAll(addStats.getPermStatuses());
		this.initialStatuses.addAll(addStats.getInitialStatuses());
	}
	
	@Override
	public UnitStats clone() {
		UnitStats unitStats = new UnitStats();
		unitStats.hp = this.hp;
		unitStats.mp = this.mp;
		unitStats.move = this.move;
		unitStats.jump = this.jump;
		unitStats.speed = this.speed;
		unitStats.pa = this.pa;
		unitStats.ma = this.ma;
		unitStats.cEv = this.cEv;
		unitStats.permStatuses = new HashSet<>(unitStats.permStatuses);
		unitStats.initialStatuses = new HashSet<>(unitStats.initialStatuses);
		
		return unitStats;
	}
	
}
