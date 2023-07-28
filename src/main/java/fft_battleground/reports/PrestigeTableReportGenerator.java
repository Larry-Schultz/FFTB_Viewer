package fft_battleground.reports;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.cache.map.AllegianceCache;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.PrestigeSkillsCache;
import fft_battleground.dump.model.PrestigeTableEntry;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import fft_battleground.reports.model.AscensionData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PrestigeTableReportGenerator extends AbstractReportGenerator<AscensionData> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.PRESTIGE_TABLE;
	private static final String reportName = "Prestige Table";
	
	@Autowired
	private PrestigeSkillsCache prestigeSkillsCache;
	
	@Autowired
	private LastActiveCache lastActiveCache;
	
	@Autowired
	private AllegianceCache allegianceCache;
	
	public PrestigeTableReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer ) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public AscensionData generateReport() throws CacheBuildException {
		Map<String, List<String>> prestigeSkillsMap = this.prestigeSkillsCache.getMap();
		List<PrestigeTableEntry> results = prestigeSkillsMap.keySet().parallelStream()
				.filter(player -> prestigeSkillsMap.get(player) != null)
				.filter(player -> !prestigeSkillsMap.get(player).isEmpty()) 
				.filter(player -> prestigeSkillsMap.get(player).size() != 417)
				.map(player -> new PrestigeTableEntry(player,
						prestigeSkillsMap.get(player).size(), 
						this.allegianceCache.get(player)))
				.collect(Collectors.toList());
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		results.stream().forEach(prestigeTableEntry -> prestigeTableEntry
				.setLastActive(format.format(this.lastActiveCache.get(prestigeTableEntry.getName()))));
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
