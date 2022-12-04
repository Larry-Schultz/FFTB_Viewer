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
import fft_battleground.dump.DumpReportsServiceImpl;
import fft_battleground.dump.service.BalanceHistoryServiceImpl;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FightPercentileReportGenerator extends AbstractReportGenerator<Map<Integer, Double>> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.FIGHT_PERCENTILES;
	private static final String reportName = "Fight Percentiles";
	
	@Autowired
	private BalanceHistoryServiceImpl balanceHistoryUtil;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	public FightPercentileReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer ) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public Map<Integer, Double> generateReport() throws CacheBuildException {
		List<PlayerRecord> allPlayerRecords = this.playerRecordRepo.findAll();
		List<Double> playerFightRatios = allPlayerRecords.stream()
		.filter(playerRecord -> playerRecord.getFightWins() != null
				&& playerRecord.getFightLosses() != null)
		.filter(playerRecord -> playerRecord.getLastActive() != null
				&& this.balanceHistoryUtil.isPlayerActiveInLastMonth(playerRecord.getLastActive()))
		.filter(playerRecord -> (playerRecord.getFightWins()
				+ playerRecord.getFightLosses()) > DumpReportsServiceImpl.PERCENTILE_THRESHOLD)
		.map(playerRecord -> ((double) playerRecord.getFightWins() + 1)
				/ ((double) playerRecord.getFightWins() + playerRecord.getFightLosses() + 1))
		.collect(Collectors.toList());
		Map<Integer, Double> percentiles = Quantiles.percentiles().indexes(IntStream.rangeClosed(0, 100).toArray())
				.compute(playerFightRatios);
		return percentiles;
	}

	@Override
	public Map<Integer, Double> deserializeJson(String json) {
		return this.deserializeMapIntegerDouble(json);
	}

}
