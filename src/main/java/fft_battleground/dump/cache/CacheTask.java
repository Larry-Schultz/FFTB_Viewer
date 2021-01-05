package fft_battleground.dump.cache;

import java.util.List;

import fft_battleground.repo.model.PlayerRecord;

public abstract class CacheTask {
	protected List<PlayerRecord> playerRecords;
	
	public CacheTask(List<PlayerRecord> playerRecords) {
		this.playerRecords = playerRecords;
	}
}