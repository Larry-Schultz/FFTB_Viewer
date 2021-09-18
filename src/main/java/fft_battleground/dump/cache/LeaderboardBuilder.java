package fft_battleground.dump.cache;

import java.util.List;

import fft_battleground.dump.DumpService;
import fft_battleground.dump.reports.ReportGenerator;
import fft_battleground.exception.DumpException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LeaderboardBuilder
implements Runnable {
	
	private DumpService dumpServiceRef;
	private List<ReportGenerator<?>> reportGenerators;
	
	public LeaderboardBuilder(DumpService dumpService) {
		this.dumpServiceRef = dumpService;
		this.reportGenerators = dumpService.getDumpReportsService().allReportGenerators();
	}
	
	@Override
	public void run() {
		// run this at startup so leaderboard data works properly
		log.info("pre-cache leaderboard data");
		try {
			this.dumpServiceRef.getDumpDataProvider().getHighScoreDump();
			this.dumpServiceRef.getDumpDataProvider().getHighExpDump();
		} catch(DumpException e) {
			log.error("error getting high score dump", e);
		}

		this.loadDatabaseData();
		this.runCacheRebuildFunctions();
		
		log.info("leaderboard data cache complete");
	}
	
	protected void loadDatabaseData() {
		this.reportGenerators.stream()
			.forEach(ReportGenerator::loadFromDatabase);
	}
	
	protected void runCacheRebuildFunctions() {
		this.reportGenerators.stream()
			.forEach(ReportGenerator::scheduleUpdates);
	}
}