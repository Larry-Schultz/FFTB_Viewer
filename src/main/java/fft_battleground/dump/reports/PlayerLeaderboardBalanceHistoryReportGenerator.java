package fft_battleground.dump.reports;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpReportsServiceImpl;
import fft_battleground.dump.reports.model.LeaderboardBalanceData;
import fft_battleground.dump.service.BalanceHistoryService;
import fft_battleground.dump.service.LeaderboardService;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.repository.BalanceHistoryRepo;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;

@Component
public class PlayerLeaderboardBalanceHistoryReportGenerator extends AbstractLeaderboardBalanceHistoryReportGenerator {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.PLAYER_LEADERBOARD_BALANCE_HISTORY;
	private static final String reportName = "Player Leaderboard Balance History";
	
	@Autowired
	private LeaderboardService leaderboardService;
	
	public PlayerLeaderboardBalanceHistoryReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer, BalanceHistoryRepo balanceHistoryRepo, BalanceHistoryService balanceHistoryService) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer, balanceHistoryRepo, balanceHistoryService);
		// TODO Auto-generated constructor stub
	}

	@Override
	public LeaderboardBalanceData generateReport() throws CacheBuildException {
		Map<String, Integer> top10 = this.leaderboardService.getTopPlayers(DumpReportsServiceImpl.HIGHEST_PLAYERS);
		Set<String> players = top10.keySet();
		LeaderboardBalanceData data = this.generatePlayerBalanceHistory(players, DEFAULT_HISTORY_COUNT);
		return data;
	}



}
