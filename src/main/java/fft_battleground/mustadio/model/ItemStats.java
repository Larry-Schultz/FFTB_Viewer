package fft_battleground.mustadio.model;

import java.util.ArrayList;
import java.util.List;

import fft_battleground.tournament.tips.Stats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemStats implements Stats, Cloneable {
	private int hp = 0;
	private int mp = 0;
    private int wp = 0;
    private int range = 0;
    private int evadePercent = 0;
    private int speed = 0;
    private String element;
    private int jump = 0;
    private int move = 0;
    private int pa = 0;
    private int absorbWp = 0;
    private int ma = 0;
    private List<Status> initialStatuses = new ArrayList<>();
    private List<Status> permStatuses = new ArrayList<>();
}
