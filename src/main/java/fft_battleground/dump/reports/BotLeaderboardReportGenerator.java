package fft_battleground.dump.reports;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.reports.model.BotLeaderboard;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;
import fft_battleground.repo.BattleGroundCacheEntryKey;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BotLeaderboardReportGenerator extends ReportGenerator<BotLeaderboard> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.BOT_LEADERBOARD;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private BattleGroundCacheEntryRepo battleGroundCacheEntryRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	public BotLeaderboardReportGenerator() {
		super(key);
	}

	@Override
	public BotLeaderboard getReport() throws CacheMissException {
		BotLeaderboard botLeaderboard = null;
		botLeaderboard = this.readCache(this.cache, key.getKey());
		if (botLeaderboard == null) {
			throw new CacheMissException(key);
		}

		return botLeaderboard;
	}

	@Override
	public BotLeaderboard writeReport() {
		BotLeaderboard botLeaderboard = null;
		try {
			log.warn("bot leaderboard cache was busted, creating new value");
			botLeaderboard = this.generateReport();
			this.writeToCache(this.cache, key.getKey(), botLeaderboard);
			this.battleGroundCacheEntryRepo.writeCacheEntry(botLeaderboard, BattleGroundCacheEntryKey.BOT_LEADERBOARD.getKey());
		} catch(Exception e) {
			log.error("Error writing to bot cache", e);
			this.errorWebhookManager.sendException(e, "exception generating new bot leaderboard");
		}
		return botLeaderboard;
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

}
