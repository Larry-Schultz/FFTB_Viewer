package fft_battleground.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fft_battleground.reports.AllegianceReportGenerator;
import fft_battleground.reports.BetPercentileReportGenerator;
import fft_battleground.reports.BotLeaderboardBalanceHistoryReportGenerator;
import fft_battleground.reports.BotLeaderboardReportGenerator;
import fft_battleground.reports.BotlandLeaderboardReportGenerator;
import fft_battleground.reports.ExperienceLeaderboardReportGenerator;
import fft_battleground.reports.FightPercentileReportGenerator;
import fft_battleground.reports.PlayerLeaderboardBalanceHistoryReportGenerator;
import fft_battleground.reports.PlayerLeaderboardReportGenerator;
import fft_battleground.reports.PlaylistSongCountHistoryReportGenerator;
import fft_battleground.reports.PlaylistSongOccurenceHistoryReportGenerator;
import fft_battleground.reports.PrestigeTableReportGenerator;
import fft_battleground.reports.ReportGenerator;
import lombok.Getter;

@Configuration
public class ReportsConfig {

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
	
	@Autowired
	@Getter private PlaylistSongCountHistoryReportGenerator playlistSongCountHistoryReportGenerator;
	
	@Autowired
	@Getter private PlaylistSongOccurenceHistoryReportGenerator playlistSongOccurenceHistoryReportGenerator;
	
	@Bean
	public List<ReportGenerator<?>> allReportGenerators() {
		List<ReportGenerator<?>> generators = List.of(this.playerLeaderboardReportGenerator, 
				this.botLeaderboardReportGenerator, this.betPercentileReportGenerator, this.fightPercentileReportGenerator, 
				this.allegianceReportGenerator, this.expLeaderboardGenerator, this.prestigeTableReportGenerator,
				this.botlandLeaderboardReportGenerator, this.playerLeaderboardBalanceHistoryReportGenerator, 
				this.botLeaderboardBalanceHistoryReportGenerator, this.playlistSongCountHistoryReportGenerator,
				this.playlistSongOccurenceHistoryReportGenerator);
		return generators;
	}
}
