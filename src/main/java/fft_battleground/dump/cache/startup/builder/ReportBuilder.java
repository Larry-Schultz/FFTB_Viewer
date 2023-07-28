package fft_battleground.dump.cache.startup.builder;

import java.util.List;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.exception.DumpException;
import fft_battleground.reports.ReportGenerator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportBuilder implements Runnable {
	private List<ReportGenerator<?>> reportGenerators;
	private DumpDataProvider dumpDataProvider;
	
	public ReportBuilder(List<ReportGenerator<?>> reportGenerators, DumpDataProvider dumpDataProvider) {
		this.reportGenerators = reportGenerators;
		this.dumpDataProvider = dumpDataProvider;
	}
	
	@Override
	public void run() {
		// run this at startup so leaderboard data works properly
		log.info("pre-cache leaderboard data");
		try {
			this.dumpDataProvider.getHighScoreDump();
			this.dumpDataProvider.getHighExpDump();
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