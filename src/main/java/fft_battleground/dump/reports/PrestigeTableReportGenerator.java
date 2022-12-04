package fft_battleground.dump.reports;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.model.PrestigeTableEntry;
import fft_battleground.dump.reports.model.AscensionData;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PrestigeTableReportGenerator extends AbstractReportGenerator<AscensionData> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.PRESTIGE_TABLE;
	private static final String reportName = "Prestige Table";
	
	@Autowired
	private DumpService dumpService;
	
	public PrestigeTableReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer ) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public AscensionData generateReport() throws CacheBuildException {
		List<PrestigeTableEntry> results = this.dumpService.getPrestigeSkillsCache().keySet().parallelStream()
				.filter(player -> this.dumpService.getPrestigeSkillsCache().get(player) != null)
				.filter(player -> !this.dumpService.getPrestigeSkillsCache().get(player).isEmpty()) 
				.filter(player -> this.dumpService.getPrestigeSkillsCache().get(player).size() != 417)
				.map(player -> new PrestigeTableEntry(player,
						this.dumpService.getPrestigeSkillsCache().get(player).size(), 
						this.dumpService.getAllegianceCache().get(player)))
				.collect(Collectors.toList());
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		results.stream().forEach(prestigeTableEntry -> prestigeTableEntry
				.setLastActive(format.format(this.dumpService.getLastActiveCache().get(prestigeTableEntry.getName()))));
		Collections.sort(results);

		AscensionData data = new AscensionData(results);
		
		return data;
	}

	@Override
	@SneakyThrows
	public AscensionData deserializeJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		AscensionData ascensionData = mapper.readValue(json, AscensionData.class);
		return ascensionData;
	}

}
