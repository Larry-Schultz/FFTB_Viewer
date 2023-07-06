package fft_battleground.metrics.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetectorAuditEntry {
	private BattleGroundEvent event;
	private ChatMessage chatMessage;
}
