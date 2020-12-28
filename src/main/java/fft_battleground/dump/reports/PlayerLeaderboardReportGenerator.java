package fft_battleground.dump.reports;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.dump.reports.model.LeaderboardData;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;
import fft_battleground.repo.BattleGroundCacheEntryKey;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PlayerLeaderboardReportGenerator extends ReportGenerator<PlayerLeaderboard> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.LEADERBOARD;
	
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
				.map(player -> this.dumpReportsService.collectPlayerLeaderboardDataByPlayer(player)).filter(result -> result != null)
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

}
