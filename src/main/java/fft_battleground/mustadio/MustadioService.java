package fft_battleground.mustadio;

import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.tournament.tips.UnitStats;

public interface MustadioService {
	UnitStats getUnitStats(UnitInfoEvent event);
	void refreshMustadioData();
}
