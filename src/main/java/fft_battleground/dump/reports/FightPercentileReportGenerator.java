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
public class FightPercentileReportGenerator extends ReportGenerator<Map<Integer, Double>> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.FIGHT_PERCENTILES;
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BattleGroundCacheEntryRepo battleGroundCacheEntryRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	public FightPercentileReportGenerator() {
		super(key);
	}

	@Override
	public Map<Integer, Double> getReport() throws CacheMissException {
		Map<Integer, Double> fightPercentiles = null;
		fightPercentiles = this.readCache(this.cache, key.getKey());
		if (fightPercentiles == null) {
			throw new CacheMissException(key);
		}

		return fightPercentiles;
	}

	@Override
	public Map<Integer, Double> writeReport() {
		log.warn("The Fight Percentiles cache was busted.  Rebuilding");
		Map<Integer, Double> fightPercentiles = null;
		try {
			fightPercentiles = this.generateReport();
			this.writeToCache(this.cache, key.getKey(), fightPercentiles);
			this.battleGroundCacheEntryRepo.writeCacheEntry(fightPercentiles, key.getKey());
			log.warn("Fight Percentiles rebuild complete");
		} catch(Exception e) {
			log.error("Error in building fight percentiles", e);
			this.errorWebhookManager.sendException(e, "Error in building fight percentiles");
		}
		
		return fightPercentiles;
	}

	@Override
	public Map<Integer, Double> generateReport() throws CacheBuildException {
		Map<Integer, Double> percentiles = Quantiles.percentiles().indexes(IntStream.rangeClosed(0, 100).toArray())
				.compute(this.playerRecordRepo.findAll().stream()
						.filter(playerRecord -> playerRecord.getFightWins() != null
								&& playerRecord.getFightLosses() != null)
						.filter(playerRecord -> playerRecord.getLastActive() != null
								&& this.dumpReportsService.isPlayerActiveInLastMonth(playerRecord.getLastActive()))
						.filter(playerRecord -> (playerRecord.getFightWins()
								+ playerRecord.getFightLosses()) > DumpReportsService.PERCENTILE_THRESHOLD)
						.map(playerRecord -> ((double) playerRecord.getFightWins() + 1)
								/ ((double) playerRecord.getFightWins() + playerRecord.getFightLosses() + 1))
						.collect(Collectors.toList()));
		return percentiles;
	}

}
