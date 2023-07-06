package fft_battleground.reports;

import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpService;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import fft_battleground.reports.model.BotLeaderboard;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BotLeaderboardReportGenerator extends AbstractReportGenerator<BotLeaderboard> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.BOT_LEADERBOARD;
	private static final String reportName = "Bot Leaderboard";
	
	@Autowired
	private DumpService dumpService;
	
	public BotLeaderboardReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer ) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public BotLeaderboard generateReport() throws CacheBuildException {
		BotLeaderboard leaderboard = null;
		Map<String, Integer> botBalances = new TreeMap<String, Integer>(this.dumpService.getBotCache().parallelStream()
				.filter(botName -> this.dumpService.getBalanceCache().containsKey(botName))
				.collect(Collectors.toMap(Function.identity(), bot -> this.dumpService.getBalanceCache().get(bot))));
		leaderboard = new BotLeaderboard(botBalances);
		return leaderboard;
	}

	@Override
	@SneakyThrows
	public BotLeaderboard deserializeJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		BotLeaderboard leaderboard = mapper.readValue(json,  BotLeaderboard.class);
		return leaderboard;
	}

}
