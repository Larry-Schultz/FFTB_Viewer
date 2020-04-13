package fft_battleground.event.detector;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.ChatMessage;

public interface EventDetector {
	public BattleGroundEvent detect(ChatMessage message); 
}