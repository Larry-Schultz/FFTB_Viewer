package fft_battleground.dump.reports;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.math.Quantiles;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BetPercentileReportGenerator extends ReportGenerator<Map<Integer, Double>> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.BET_PERCENTILES;
	private static final String reportName = "Bet Percentiles";
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	public BetPercentileReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public Map<Integer, Double> generateReport() throws CacheBuildException {
		List<PlayerRecord> allPlayers = this.playerRecordRepo.findAll();
		List<Double> ratios = allPlayers.stream()
				.filter(playerRecord -> playerRecord.getWins() != null && playerRecord.getLosses() != null)
				.filter(playerRecord -> playerRecord.getLastActive() != null
						&& this.dumpReportsService.isPlayerActiveInLastMonth(playerRecord.getLastActive()))
				.filter(playerRecord -> (playerRecord.getWins()
						+ playerRecord.getLosses()) > DumpReportsService.PERCENTILE_THRESHOLD)
				.map(playerRecord -> ((double) playerRecord.getWins() + 1)
						/ ((double) playerRecord.getWins() + playerRecord.getLosses() + 1))
				.collect(Collectors.toList());
		Map<Integer, Double> percentiles = Quantiles.percentiles().indexes(IntStream.rangeClosed(0, 100).toArray())
				.compute(ratios);
		return percentiles;
	}

	@Override
	public Map<Integer, Double> deserializeJson(String json) {
		return this.deserializeMapIntegerDouble(json);
	}

}
