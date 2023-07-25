package fft_battleground.scheduled.tasks.daily;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpService;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.dao.RepoTransactionManager;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BadAccountsDailyTask extends DumpDailyScheduledTask {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private RepoTransactionManager repoTransactionManager;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	public BadAccountsDailyTask(@Autowired DumpService dumpService) { 
		super(dumpService);
	}
	
	protected void task() {
		this.generateOutOfSyncPlayerRecordsFile();
	}
	
	private void generateOutOfSyncPlayerRecordsFile() {
		log.info("starting out of sync player record file batch");
		try {
			Map<String, Integer> balanceMap = this.dumpDataProvider.getHighScoreDump();
			List<String> realPlayers = balanceMap.keySet().parallelStream().map(key -> StringUtils.lowerCase(key)).collect(Collectors.toList());
			List<PlayerRecord> currentAccounts = this.playerRecordRepo.findAllPlayerNames();
			
			List<String> badAccounts = currentAccounts.parallelStream().map(playerRecord -> playerRecord.getPlayer())
					.filter(account -> !realPlayers.contains(account))
					.collect(Collectors.toList());
			List<String> falselyFlaggedAccounts = currentAccounts.parallelStream().filter(playerRecord -> !playerRecord.getIsActive())
					.map(playerRecord -> playerRecord.getPlayer())
					.filter(player -> realPlayers.contains(player))
					.collect(Collectors.toList());
			
			this.repoTransactionManager.softDeletePlayerAccount(badAccounts);
			log.info("soft deleted {} accounts", badAccounts.size());
			this.repoTransactionManager.undeletePlayerAccounts(falselyFlaggedAccounts);
			log.info("undeleted {} accounts", falselyFlaggedAccounts.size());
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter("badAccounts.txt"))) {
				for(String badAccountName : badAccounts) {
					writer.write(badAccountName);
					writer.newLine();
				}
			}
		} catch(IOException | DumpException e) {
			log.error("Error writing bad accounts file");
		}
		
		log.info("finished writing bad accounts file");
	}
}