package fft_battleground.event.annotate;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.BattleGroundException;

public interface BattleGroundEventAnnotator<T extends BattleGroundEvent> {

	public void annotateEvent(T event) throws BattleGroundException;
}
