package fft_battleground.dump;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.dump.reports.AllegianceReportGenerator;
import fft_battleground.dump.reports.BetPercentileReportGenerator;
import fft_battleground.dump.reports.BotLeaderboardBalanceHistoryReportGenerator;
import fft_battleground.dump.reports.BotLeaderboardReportGenerator;
import fft_battleground.dump.reports.BotlandLeaderboardReportGenerator;
import fft_battleground.dump.reports.ExperienceLeaderboardReportGenerator;
import fft_battleground.dump.reports.FightPercentileReportGenerator;
import fft_battleground.dump.reports.PlayerLeaderboardBalanceHistoryReportGenerator;
import fft_battleground.dump.reports.PlayerLeaderboardReportGenerator;
import fft_battleground.dump.reports.PrestigeTableReportGenerator;
import fft_battleground.dump.reports.ReportGenerator;
import fft_battleground.dump.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.dump.reports.model.AscensionData;
import fft_battleground.dump.reports.model.BotLeaderboard;
import fft_battleground.dump.reports.model.ExpLeaderboard;
import fft_battleground.dump.reports.model.LeaderboardBalanceData;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.exception.CacheMissException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DumpReportsServiceImpl implements DumpReportsService {

	public static final int HIGHEST_PLAYERS = 10;
	public static final int TOP_PLAYERS = 100;
	public static final int PERCENTILE_THRESHOLD = 10;

	@Autowired
	@Getter private PlayerLeaderboardReportGenerator playerLeaderboardReportGenerator;
	
	@Autowired
	@Getter private BotLeaderboardReportGenerator botLeaderboardReportGenerator;
	
	@Autowired
	@Getter private BetPercentileReportGenerator betPercentileReportGenerator;
	
	@Autowired
	@Getter private FightPercentileReportGenerator fightPercentileReportGenerator;
	
	@Autowired
	@Getter private AllegianceReportGenerator allegianceReportGenerator;
	
	@Autowired
	@Getter private ExperienceLeaderboardReportGenerator expLeaderboardGenerator;
	
	@Autowired
	@Getter private PrestigeTableReportGenerator prestigeTableReportGenerator;
	
	@Autowired
	@Getter private BotlandLeaderboardReportGenerator botlandLeaderboardReportGenerator;
	
	@Autowired
	@Getter private PlayerLeaderboardBalanceHistoryReportGenerator playerLeaderboardBalanceHistoryReportGenerator;
	
	@Autowired
	@Getter private BotLeaderboardBalanceHistoryReportGenerator botLeaderboardBalanceHistoryReportGenerator;
	
	@Override
	public List<ReportGenerator<?>> allReportGenerators() {
		List<ReportGenerator<?>> generators = List.of(this.playerLeaderboardReportGenerator, 
				this.botLeaderboardReportGenerator, this.betPercentileReportGenerator, this.fightPercentileReportGenerator, 
				this.allegianceReportGenerator, this.expLeaderboardGenerator, this.prestigeTableReportGenerator,
				this.botlandLeaderboardReportGenerator, this.playerLeaderboardBalanceHistoryReportGenerator, 
				this.botLeaderboardBalanceHistoryReportGenerator);
		return generators;
	}

	@Override
	public BotLeaderboard getBotLeaderboard() throws CacheMissException {
		BotLeaderboard result = this.botLeaderboardReportGenerator.getReport();
		return result;
	}
	
	@Override
	public BotLeaderboard writeBotLeaderboardToCaches() {
		BotLeaderboard result = this.botLeaderboardReportGenerator.writeReport();
		return result;
	}

	@Override
	public PlayerLeaderboard getLeaderboard() throws CacheMissException {
		PlayerLeaderboard leaderboard = this.playerLeaderboardReportGenerator.getReport();
		return leaderboard;
	}
	
	@Override
	public PlayerLeaderboard writeLeaderboard() {
		PlayerLeaderboard leaderboard = this.playerLeaderboardReportGenerator.writeReport();
		return leaderboard;
	}

	@Override
	public AscensionData generatePrestigeTable() {
		AscensionData ascensionData;
		try {
			ascensionData = this.prestigeTableReportGenerator.getReport();
		} catch (CacheMissException e) {
			log.warn("cache miss for prestige table", e);
			ascensionData = this.prestigeTableReportGenerator.writeReport();
		}
		return ascensionData;
	}

	@Override
	public Integer getBetPercentile(Double ratio) throws CacheMissException {
		Map<Integer, Double> betPercentiles = this.betPercentileReportGenerator.getReport();

		Integer result = null;
		for (Map.Entry<Integer, Double> entry: betPercentiles.entrySet()) {
			Double currentPercentile = entry.getValue();
			try {
				if (ratio < currentPercentile) {
					Integer key = Integer.valueOf(entry.getKey());
					result = key - 1;
					break;
				}
			}catch(NullPointerException e) {
				log.error("NullPointerException caught", e);
			} catch(ClassCastException e) {
				log.error("ClassCast exception caught", e);
			}
		}

		return result;
	}
	
	@Override
	public Map<Integer, Double> writeBetPercentile() {
		Map<Integer, Double> betPercentiles = this.betPercentileReportGenerator.writeReport();
		return betPercentiles;
	}

	@Override
	public Integer getFightPercentile(Double ratio) throws CacheMissException {
		Map<Integer, Double> fightPercentiles = this.fightPercentileReportGenerator.getReport();

		Integer result = null;
		for (int i = 0; result == null && i <= 100; i++) {
			Double currentPercentile = fightPercentiles.get(i);
			if (ratio < currentPercentile) {
				result = i - 1;
			}
		}

		return result;
	}
	
	@Override
	public Map<Integer, Double> writeFightPercentile() {
		Map<Integer, Double> fightPercentiles = this.fightPercentileReportGenerator.writeReport();
		return fightPercentiles;
	}

	@Override
	public AllegianceLeaderboardWrapper getAllegianceData() throws CacheMissException {
		AllegianceLeaderboardWrapper allegianceLeaderboardWrapper = this.allegianceReportGenerator.getReport();
		return allegianceLeaderboardWrapper;
	}
	
	@Override
	public AllegianceLeaderboardWrapper writeAllegianceWrapper() {
		AllegianceLeaderboardWrapper wrapper = this.allegianceReportGenerator.writeReport();
		return wrapper;
	}
	
	@Override
	public ExpLeaderboard getExpLeaderboard() throws CacheMissException {
		ExpLeaderboard leaderboard = this.expLeaderboardGenerator.getReport();
		return leaderboard;
	}
	
	@Override
	public ExpLeaderboard writeExpLeaderboard() {
		ExpLeaderboard leaderboard = this.expLeaderboardGenerator.writeReport();
		return leaderboard;
	}

	@Override
	public LeaderboardBalanceData getPlayerLeaderboardBalanceHistory() throws CacheMissException {
		LeaderboardBalanceData playerLeaderboardBalanceHistory = this.playerLeaderboardBalanceHistoryReportGenerator.getReport();
		return playerLeaderboardBalanceHistory;
	}

	@Override
	public LeaderboardBalanceData getBotLeaderboardBalanceHistory() throws CacheMissException {
		LeaderboardBalanceData botLeaderboardBalanceHistory = this.botLeaderboardBalanceHistoryReportGenerator.getReport();
		return botLeaderboardBalanceHistory;
	}

}

