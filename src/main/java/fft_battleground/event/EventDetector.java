package fft_battleground.event;

import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.model.ChatMessage;

public interface EventDetector<T extends BattleGroundEvent> {
	public T detect(ChatMessage message); 
}