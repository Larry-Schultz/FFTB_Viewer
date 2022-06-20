package fft_battleground.mustadio.model;

import java.util.Collections;
import java.util.List;

import fft_battleground.tournament.tips.Stats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassBaseStats implements Stats, Cloneable {
    public int hp;
    public int mp;
    public int move;
    public int jump;
    public int speed;
    public int pa;
    public int ma;
    public int cEvPercent;
	
    @Override
	public List<Status> getPermStatuses() {
		return Collections.emptyList();
	}
    
	@Override
	public List<Status> getInitialStatuses() {
		return Collections.emptyList();
	}
}
