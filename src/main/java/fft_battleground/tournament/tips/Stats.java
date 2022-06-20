package fft_battleground.tournament.tips;

import java.util.Collection;
import fft_battleground.mustadio.model.Status;

public interface Stats {
	int getHp();
	int getMp();
	int getMove();
	int getJump();
	int getSpeed();
	int getPa();
	int getMa();
	Collection<Status> getPermStatuses();
	Collection<Status> getInitialStatuses();
}
