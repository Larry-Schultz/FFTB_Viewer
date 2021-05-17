package fft_battleground.dump.reports;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.math.Quantiles;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BetPercentileReportGenerator extends ReportGenerator<Map<Integer, Double>> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.BET_PERCENTILES;
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BattleGroundCacheEntryRepo battleGroundCacheEntryRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	public BetPercentileReportGenerator() {
		super(key);
	}

	@Override
	public Map<Integer, Double> getReport() throws CacheMissException {
		Map<Integer, Double> betPercentiles = this.readCache(this.cache, key.getKey());
		if (betPercentiles == null) {
			throw new CacheMissException(key);
		}
		
		return betPercentiles;
	}

	@Override
	public Map<Integer, Double> writeReport() {
		log.warn("The Bet Percentiles cache was busted.  Rebuilding");
		Map<Integer, Double> betPercentiles = null;
		try {
			betPercentiles = this.generateReport();
			this.writeToCache(this.cache, key.getKey(), betPercentiles);
			this.battleGroundCacheEntryRepo.writeCacheEntry(betPercentiles, key.getKey());
			log.warn("Bet Percentiles rebuild complete");
		} catch(Exception e) {
			log.error("Error in building bet percentiles", e);
			this.errorWebhookManager.sendException(e, "Error in building bet percentiles");
		}
		
		return betPercentiles;
	}

	@Override
	public Map<Integer, Double> generateReport() throws CacheBuildException {
		Map<Integer, Double> percentiles = Quantiles.percentiles().indexes(IntStream.rangeClosed(0, 100).toArray())
				.compute(this.playerRecordRepo.findAll().stream()
						.filter(playerRecord -> playerRecord.getWins() != null && playerRecord.getLosses() != null)
						.filter(playerRecord -> playerRecord.getLastActive() != null
								&& this.dumpReportsService.isPlayerActiveInLastMonth(playerRecord.getLastActive()))
						.filter(playerRecord -> (playerRecord.getWins()
								+ playerRecord.getLosses()) > DumpReportsService.PERCENTILE_THRESHOLD)
						.map(playerRecord -> ((double) playerRecord.getWins() + 1)
								/ ((double) playerRecord.getWins() + playerRecord.getLosses() + 1))
						.collect(Collectors.toList()));
		return percentiles;
	}

}
