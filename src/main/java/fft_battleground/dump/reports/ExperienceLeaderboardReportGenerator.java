package fft_battleground.dump.reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.reports.model.ExpLeaderboard;
import fft_battleground.dump.reports.model.ExpLeaderboardEntry;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExperienceLeaderboardReportGenerator extends ReportGenerator<ExpLeaderboard> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.EXPERIENCE_LEADERBOARD;
	private static final String reportName = "Experience Leaderboard";
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	public ExperienceLeaderboardReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer ) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public ExpLeaderboard generateReport() throws CacheBuildException {
		List<ExpLeaderboardEntry> results = new ArrayList<>();
		for (int rank = 1; rank <= DumpReportsService.TOP_PLAYERS; rank++) {
			ExpLeaderboardEntry result = null;
			String player = this.dumpService.getExpRankLeaderboardByRank().get(rank);
			Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(player);
			if (maybePlayer.isPresent() && this.dumpReportsService.isPlayerActiveInLastMonth(maybePlayer.get().getLastActive())) {
				Short level = maybePlayer.get().getLastKnownLevel();
				Short exp = maybePlayer.get().getLastKnownRemainingExp();
				SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
				String lastActive = format.format(maybePlayer.get().getLastActive());
				BattleGroundTeam team = maybePlayer.get().getAllegiance();
				if(team == null) {
					team = BattleGroundTeam.NONE;
				}

				Integer prestigeLevel = 0;
				List<String> prestigeSkills = this.dumpService.getPrestigeSkillsCache().get(player);
				if (prestigeSkills != null) {
					prestigeLevel = prestigeSkills.size();
				}

				result = new ExpLeaderboardEntry(rank, player, level, exp, prestigeLevel, lastActive, team);
				results.add(result);
			}
		}
		
		ExpLeaderboard leaderboard = new ExpLeaderboard(results);

		return leaderboard;
	}

	@Override
	@SneakyThrows
	public ExpLeaderboard deserializeJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		ExpLeaderboard leaderboard = mapper.readValue(json, ExpLeaderboard.class);
		return leaderboard;
	}



}
