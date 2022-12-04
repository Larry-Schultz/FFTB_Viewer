package fft_battleground.dump;

import java.util.List;
import java.util.Map;

import fft_battleground.dump.reports.ReportGenerator;
import fft_battleground.dump.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.dump.reports.model.AscensionData;
import fft_battleground.dump.reports.model.BotLeaderboard;
import fft_battleground.dump.reports.model.ExpLeaderboard;
import fft_battleground.dump.reports.model.LeaderboardBalanceData;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.exception.CacheMissException;

public interface DumpReportsService {
	List<ReportGenerator<?>> allReportGenerators();
	BotLeaderboard getBotLeaderboard() throws CacheMissException;
	BotLeaderboard writeBotLeaderboardToCaches();
	PlayerLeaderboard getLeaderboard() throws CacheMissException;
	PlayerLeaderboard writeLeaderboard();
	AscensionData generatePrestigeTable();
	Integer getBetPercentile(Double ratio) throws CacheMissException;
	Map<Integer, Double> writeBetPercentile();
	Integer getFightPercentile(Double ratio) throws CacheMissException;
	Map<Integer, Double> writeFightPercentile();
	AllegianceLeaderboardWrapper getAllegianceData() throws CacheMissException;
	AllegianceLeaderboardWrapper writeAllegianceWrapper();
	ExpLeaderboard getExpLeaderboard() throws CacheMissException;
	ExpLeaderboard writeExpLeaderboard();
	LeaderboardBalanceData getPlayerLeaderboardBalanceHistory() throws CacheMissException;
	LeaderboardBalanceData getBotLeaderboardBalanceHistory() throws CacheMissException;
}
