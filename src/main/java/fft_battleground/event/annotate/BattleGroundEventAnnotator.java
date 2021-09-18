package fft_battleground.event.annotate;

import fft_battleground.event.model.BattleGroundEvent;

public interface BattleGroundEventAnnotator<T extends BattleGroundEvent> {

	public void annotateEvent(T event);
}
