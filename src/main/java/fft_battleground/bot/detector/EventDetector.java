package fft_battleground.bot.detector;

import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.model.ChatMessage;

public interface EventDetector {
	public BattleGroundEvent detect(ChatMessage message); 
}