package fft_battleground.dump.reports;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpReportsServiceImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.reports.model.LeaderboardData;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.dump.service.GlobalGiServiceImpl;
import fft_battleground.dump.service.LeaderboardService;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PlayerLeaderboardReportGenerator extends AbstractReportGenerator<PlayerLeaderboard> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.LEADERBOARD;
	private static final String reportName = "Player Leaderboard";
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private LeaderboardService leaderboardService;
	
	@Autowired
	private GlobalGiServiceImpl globalGilUtil;
	
	public PlayerLeaderboardReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer ) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public PlayerLeaderboard generateReport() throws CacheBuildException {
		Map<String, Integer> topPlayers = this.leaderboardService.getTopPlayers(DumpReportsServiceImpl.TOP_PLAYERS);
		List<LeaderboardData> allPlayers = topPlayers.keySet().parallelStream()
				.map(player -> this.collectPlayerLeaderboardDataByPlayer(player)).filter(result -> result != null)
				.sorted().collect(Collectors.toList());
		Collections.reverse(allPlayers);
		for (int i = 0; i < allPlayers.size(); i++) {
			allPlayers.get(i).setRank(i + 1);
		}

		List<LeaderboardData> highestPlayers = allPlayers.parallelStream()
				.filter(leaderboardData -> leaderboardData.getRank() <= DumpReportsServiceImpl.HIGHEST_PLAYERS).collect(Collectors.toList());
		List<LeaderboardData> topPlayersList = allPlayers.parallelStream()
				.filter(leaderboardData -> leaderboardData.getRank() > DumpReportsServiceImpl.HIGHEST_PLAYERS
						&& leaderboardData.getRank() <= DumpReportsServiceImpl.TOP_PLAYERS)
				.collect(Collectors.toList());
		PlayerLeaderboard leaderboard = new PlayerLeaderboard(highestPlayers, topPlayersList);

		return leaderboard;
	}

	@Override
	@SneakyThrows
	public PlayerLeaderboard deserializeJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		PlayerLeaderboard leaderboard;
		leaderboard = mapper.readValue(json, PlayerLeaderboard.class);
		
		return leaderboard;
	}
	
	@SneakyThrows
	public LeaderboardData collectPlayerLeaderboardDataByPlayer(String player) {
		NumberFormat myFormat = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		SimpleDateFormat dateFormat = new SimpleDateFormat(LeaderboardData.LEADERBOARD_ACTIVE_PLAYER_DATE_FORMAT);
		DecimalFormat decimalFormat = new DecimalFormat("##.#########");

		LeaderboardData data = null;
		Integer gil = this.dumpService.getBalanceFromCache(player);
		Date lastActive = this.dumpService.getLastActiveDateFromCache(player);
		BattleGroundTeam allegiance = this.dumpService.getAllegianceCache().get(player);
		if(allegiance == null) {
			allegiance = BattleGroundTeam.NONE;
		}

		String gilString = myFormat.format(gil);
		String percentageOfGlobalGil = decimalFormat.format(this.globalGilUtil.percentageOfGlobalGil(gil) * (double) 100);
		String activeDate = dateFormat.format(lastActive);
		data = new LeaderboardData(player, gilString, activeDate);
		data.setPercentageOfGlobalGil(percentageOfGlobalGil);
		data.setAllegiance(allegiance);

		return data;
	}

}
