package fft_battleground.dump.reports;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.reports.model.LeaderboardData;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;
import fft_battleground.repo.BattleGroundCacheEntryKey;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PlayerLeaderboardReportGenerator extends ReportGenerator<PlayerLeaderboard> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.LEADERBOARD;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
	@Autowired
	private BattleGroundCacheEntryRepo battleGroundCacheEntryRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	public PlayerLeaderboardReportGenerator() {
		super(key);
	}

	@Override
	public PlayerLeaderboard getReport() throws CacheMissException {
		PlayerLeaderboard leaderboard = this.readCache(this.cache, BattleGroundCacheEntryKey.LEADERBOARD.getKey());
		if (leaderboard == null) {
			throw new CacheMissException(BattleGroundCacheEntryKey.LEADERBOARD);
		}
		return leaderboard;
	}

	@Override
	public PlayerLeaderboard writeReport() {
		log.warn("Leaderboard cache was busted, creating new value");
		PlayerLeaderboard leaderboard = null;
		try {
			leaderboard = this.generateReport();
			this.writeToCache(this.cache, key.getKey(), leaderboard);
			this.battleGroundCacheEntryRepo.writeCacheEntry(leaderboard, key.getKey());
		} catch(Exception e) {
			log.error("Error writing to bot cache", e);
			this.errorWebhookManager.sendException(e, "exception generating new player leaderboard");
		}
		return leaderboard;
	}

	@Override
	public PlayerLeaderboard generateReport() throws CacheBuildException {
		Map<String, Integer> topPlayers = this.dumpReportsService.getTopPlayers(DumpReportsService.TOP_PLAYERS);
		List<LeaderboardData> allPlayers = topPlayers.keySet().parallelStream()
				.map(player -> this.collectPlayerLeaderboardDataByPlayer(player)).filter(result -> result != null)
				.sorted().collect(Collectors.toList());
		Collections.reverse(allPlayers);
		for (int i = 0; i < allPlayers.size(); i++) {
			allPlayers.get(i).setRank(i + 1);
		}

		List<LeaderboardData> highestPlayers = allPlayers.parallelStream()
				.filter(leaderboardData -> leaderboardData.getRank() <= DumpReportsService.HIGHEST_PLAYERS).collect(Collectors.toList());
		List<LeaderboardData> topPlayersList = allPlayers.parallelStream()
				.filter(leaderboardData -> leaderboardData.getRank() > DumpReportsService.HIGHEST_PLAYERS
						&& leaderboardData.getRank() <= DumpReportsService.TOP_PLAYERS)
				.collect(Collectors.toList());
		PlayerLeaderboard leaderboard = new PlayerLeaderboard(highestPlayers, topPlayersList);

		return leaderboard;
	}
	
	@SneakyThrows
	public LeaderboardData collectPlayerLeaderboardDataByPlayer(String player) {
		NumberFormat myFormat = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		DecimalFormat decimalFormat = new DecimalFormat("##.#########");

		LeaderboardData data = null;
		Integer gil = this.dumpService.getBalanceFromCache(player);
		Date lastActive = this.dumpService.getLastActiveDateFromCache(player);

		String gilString = myFormat.format(gil);
		String percentageOfGlobalGil = decimalFormat.format(this.dumpReportsService.percentageOfGlobalGil(gil) * (double) 100);
		String activeDate = dateFormat.format(lastActive);
		data = new LeaderboardData(player, gilString, activeDate);
		data.setPercentageOfGlobalGil(percentageOfGlobalGil);

		return data;
	}

}
