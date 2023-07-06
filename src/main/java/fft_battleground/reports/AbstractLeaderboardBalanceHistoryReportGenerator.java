package fft_battleground.reports;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.service.BalanceHistoryService;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.repository.BalanceHistoryRepo;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import fft_battleground.reports.model.LeaderboardBalanceData;
import fft_battleground.reports.model.LeaderboardBalanceHistoryEntry;
import lombok.SneakyThrows;

public abstract class AbstractLeaderboardBalanceHistoryReportGenerator extends AbstractReportGenerator<LeaderboardBalanceData> {

	protected static final int DEFAULT_HISTORY_COUNT = 10;
	protected static final int MINIMUM_HISTORY_COUNT = 2;
	
	private BalanceHistoryRepo balanceHistoryRepo;
	
	private BalanceHistoryService balanceHistoryService;
	
	public AbstractLeaderboardBalanceHistoryReportGenerator(BattleGroundCacheEntryKey key, String reportName,
			BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager,
			Timer battleGroundCacheTimer, BalanceHistoryRepo balanceHistoryRepo, BalanceHistoryService balanceHistoryService) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battleGroundCacheTimer);
		this.balanceHistoryRepo = balanceHistoryRepo;
		this.balanceHistoryService = balanceHistoryService;
		// TODO Auto-generated constructor stub
	}

	@Override
	public abstract LeaderboardBalanceData generateReport() throws CacheBuildException;

	@Override
	@SneakyThrows
	public LeaderboardBalanceData deserializeJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		LeaderboardBalanceData ascensionData = mapper.readValue(json, LeaderboardBalanceData.class);
		return ascensionData;
	}
	
	protected LeaderboardBalanceData generatePlayerBalanceHistory(Collection<String> playerList, int count) {
		Map<String, List<BalanceHistory>> playerBalanceHistories = playerList.parallelStream()
				.collect(Collectors.toMap(Function.identity(), playerName -> new LinkedList<BalanceHistory>(this.balanceHistoryRepo.getTournamentBalanceHistoryFromPastWeek(playerName))));
		
		List<LeaderboardBalanceHistoryEntry> playerBalanceHistoryEntries = playerBalanceHistories.keySet().parallelStream().map(playerName -> new LeaderboardBalanceHistoryEntry(playerName, playerBalanceHistories.get(playerName)))
				.collect(Collectors.toList());
		
		int consensusCount = this.historyConsensusCount(playerBalanceHistories, count);
		
		LeaderboardBalanceData data = this.balanceHistoryService.getLabelsAndSetRelevantBalanceHistories(playerBalanceHistoryEntries, consensusCount);
		data.setStandardSize(consensusCount);
		
		return data;
	}
	
	protected int historyConsensusCount(Map<String, List<BalanceHistory>> balanceHistoriesMap, int maxHistoryEntries) {
		int count = balanceHistoriesMap.keySet().parallelStream()
				.map(playerName -> new LeaderboardBalanceHistoryEntry(playerName, balanceHistoriesMap.get(playerName)))
				.mapToInt(leaderboardBalanceHistory -> leaderboardBalanceHistory.getBalanceHistory().size())
				.filter(size -> size >= MINIMUM_HISTORY_COUNT)
				.min().orElse(MINIMUM_HISTORY_COUNT);
		if(count > maxHistoryEntries) {
			count = maxHistoryEntries;
		}
		
		return count;
	}

}
