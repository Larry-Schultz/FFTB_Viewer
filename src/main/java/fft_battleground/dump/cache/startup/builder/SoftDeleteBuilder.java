package fft_battleground.dump.cache.startup.builder;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import fft_battleground.repo.repository.PlayerRecordRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SoftDeleteBuilder
implements Callable<Set<String>> {
	private PlayerRecordRepo playerRecordRepo;
	
	public SoftDeleteBuilder(PlayerRecordRepo playerRecordRepo) {
		this.playerRecordRepo = playerRecordRepo;
	}
	
	@Override
	public Set<String> call() throws Exception {
		log.info("starting soft delete cache build");
		List<String> softDeletedPlayerList = this.playerRecordRepo.findSoftDeletedPlayers();
		Set<String> softDeletedPlayers = Sets.newConcurrentHashSet(softDeletedPlayerList);
		log.info("soft delete cache build complete");
		return softDeletedPlayers;
	}
	
}