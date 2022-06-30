package fft_battleground.dump.cache.task;

import java.util.List;
import java.util.concurrent.Callable;

import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;

public class PlayerRecordFindTask 
implements Callable<List<PlayerRecord>> {
	
	private List<String> players;
	private PlayerRecordRepo playerRepo;
	
	public PlayerRecordFindTask(List<String> playerNames, PlayerRecordRepo playerRepo) {
		this.players = playerNames;
		this.playerRepo = playerRepo;
	}

	@Override
	public List<PlayerRecord> call() throws Exception {
		List<PlayerRecord> result = this.playerRepo.findAllById(players);
		return result;
	}
}