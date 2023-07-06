package fft_battleground.reports;

import java.util.List;
import java.util.Timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.service.BalanceHistoryService;
import fft_battleground.dump.service.LeaderboardService;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.repository.BalanceHistoryRepo;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import fft_battleground.reports.model.LeaderboardBalanceData;

@Component
public class BotLeaderboardBalanceHistoryReportGenerator extends AbstractLeaderboardBalanceHistoryReportGenerator {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.BOT_LEADERBOARD_BALANCE_HISTORY;
	private static final String reportName = "Bot Leaderboard Balance History";
	
	@Autowired
	private LeaderboardService leaderboardService;
	
	public BotLeaderboardBalanceHistoryReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager,
			Timer battleGroundCacheTimer, BalanceHistoryRepo balanceHistoryRepo,
			BalanceHistoryService balanceHistoryService) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battleGroundCacheTimer, balanceHistoryRepo,
				balanceHistoryService);
		// TODO Auto-generated constructor stub
	}

	@Override
	public LeaderboardBalanceData generateReport() throws CacheBuildException {
		List<String> activeBots = this.leaderboardService.getTopActiveBots(TOP_PLAYERS);
		LeaderboardBalanceData data = this.generatePlayerBalanceHistory(activeBots, DEFAULT_HISTORY_COUNT);
		return data;
	}

}
