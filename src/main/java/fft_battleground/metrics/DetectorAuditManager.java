package fft_battleground.metrics;

import fft_battleground.event.model.BattleGroundEventType;

public interface DetectorAuditManager {
	public void addEvent(BattleGroundEventType type);
	public void updateDatabaseAndClearCache();
}
