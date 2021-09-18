package fft_battleground.dump.reports;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.reports.model.BotlandLeaderboard;
import fft_battleground.dump.reports.model.BotlandWinner;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.model.Bots;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.BotsRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BotlandLeaderboardReportGenerator extends ReportGenerator<BotlandLeaderboard> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.BOTLAND_LEADERBOARD;
	private static final String reportName = "Botland Leaderboard";
	
	@Autowired
	private BotsRepo botsRepo;
	
	public BotlandLeaderboardReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public BotlandLeaderboard generateReport() throws CacheBuildException {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
		
		BotlandLeaderboard leaderboard = null;
		
		Future<List<Bots>> botList = executor.submit(() -> this.botsRepo.getBotsForToday()); 
		Future<List<Bots>> botEntriesWithHighestKnownValueAllTime = executor.submit(() -> this.botsRepo.highestKnownValueHistorical());
		Future<List<Bots>> botEntriesThatAreWinnersForDay = executor.submit(() -> this.botsRepo.highestBalancePerDay());
		Future<List<Bots>> oldestEntriesPerBot = executor.submit(() -> this.botsRepo.getOldestEntries());
		Future<List<Pair<String, Long>>> winsPerBot = executor.submit(() -> this.botsRepo.getWinsPerBot());
		Future<List<Pair<String, Long>>> lossesPerBot = executor.submit(() -> this.botsRepo.getLossesPerBot());
		
		try {
			//winner for past 100 days
			Map<String, List<Bots>> dateStringWinnerBotsMap = botEntriesThatAreWinnersForDay.get().stream()
					.sorted(Comparator.comparing(Bots::getUpdateDateTime).reversed())
					.limit(100)
					.collect(Collectors.groupingBy(Bots::getDateString));
			Function<String, BotlandWinner> botlandWinnerFunction = (key) -> new BotlandWinner(key, dateStringWinnerBotsMap.get(key).stream().map(Bots::getPlayer).collect(Collectors.joining(", ")), 
					dateStringWinnerBotsMap.get(key).get(0).getBalance());
			Map<String, BotlandWinner> dateStringWinnersMap = dateStringWinnerBotsMap.keySet().stream()
					.filter(key -> !dateStringWinnerBotsMap.get(key).isEmpty())
					.collect(Collectors.toMap(Function.identity(), botlandWinnerFunction));

			//determine highest of all time for each bot
			Map<String, Integer> botHighestOfAllTimeMap = botEntriesWithHighestKnownValueAllTime.get().stream()
					.collect(Collectors.toMap(bot -> bot.getPlayer(), bot -> bot.getHighestKnownValue()));
			
			//determine number of wins per bot
			Map<String, Long> botWinCountMap = botEntriesThatAreWinnersForDay.get().stream()
					.collect(Collectors.groupingBy(Bots::getPlayer, Collectors.counting()));
			
			//determine oldest entry per bot
			Map<String, Bots> oldestBotEntryMap = oldestEntriesPerBot.get().stream()
					.collect(Collectors.toMap(Bots::getPlayer, Function.identity()));
			
			Map<String, Long> winCountsPerBot = winsPerBot.get().stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
			Map<String, Long> lossCountsPerBot = lossesPerBot.get().stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
			
			leaderboard = new BotlandLeaderboard(botList.get(), dateStringWinnersMap, botHighestOfAllTimeMap, botWinCountMap, oldestBotEntryMap, winCountsPerBot,
					lossCountsPerBot);
		} catch (InterruptedException|ExecutionException e) {
			String errorMessage = "Error building Botland Leaderboard cache";
			log.error("Error building Botland Leaderboard cache", e);
			throw new CacheBuildException(errorMessage, e);
		}
		
		return leaderboard;
	}

	@Override
	@SneakyThrows
	public BotlandLeaderboard deserializeJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		BotlandLeaderboard leaderboard = mapper.readValue(json,  BotlandLeaderboard.class);
		return leaderboard;
	}

}
