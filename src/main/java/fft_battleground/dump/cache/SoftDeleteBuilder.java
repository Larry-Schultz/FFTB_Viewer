package fft_battleground.dump.cache;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import fft_battleground.dump.DumpService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SoftDeleteBuilder
implements Callable<Set<String>> {
	private DumpService dumpServiceRef;
	
	public SoftDeleteBuilder(DumpService dumpServiceRef) {
		this.dumpServiceRef = dumpServiceRef;
	}
	
	@Override
	public Set<String> call() throws Exception {
		log.info("starting soft delete cache build");
		List<String> softDeletedPlayerList = this.dumpServiceRef.getPlayerRecordRepo().findSoftDeletedPlayers();
		Set<String> softDeletedPlayers = Sets.newConcurrentHashSet(softDeletedPlayerList);
		log.info("soft delete cache build complete");
		return softDeletedPlayers;
	}
	
}