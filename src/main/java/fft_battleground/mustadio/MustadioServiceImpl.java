package fft_battleground.mustadio;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.discord.WebhookManager;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.exception.MustadioApiException;
import fft_battleground.model.Gender;
import fft_battleground.mustadio.model.ClassBaseStats;
import fft_battleground.mustadio.model.ClassData;
import fft_battleground.mustadio.model.Item;
import fft_battleground.mustadio.model.ItemStats;
import fft_battleground.tournament.model.Unit;
import fft_battleground.tournament.tips.Stats;
import fft_battleground.tournament.tips.UnitStats;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MustadioServiceImpl implements MustadioService {

	@Autowired
	private MustadioRestService mustadioRestService;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private WebhookManager noisyWebhookManager;
	
	private Map<String, ItemStats> itemStatsMap;
	private Map<ClassDataKey, ClassBaseStats> classBaseStats;
	
	@Override
	public UnitStats getUnitStats(UnitInfoEvent event) {
		Unit unit = event.getUnit();
		List<StatsData> statList = new ArrayList<>();
		statList.add(this.getBaseClassStats(unit));
		if(StringUtils.isNotBlank(unit.getMainhand())) {
			statList.add(this.getItemStats(unit.getMainhand(), StatsType.MAIN_HAND));
		}
		if(StringUtils.isNotBlank(unit.getOffhand())) {
			statList.add(this.getItemStats(unit.getOffhand(), StatsType.OFF_HAND));
		}
		if(StringUtils.isNotBlank(unit.getHead())) {
			statList.add(this.getItemStats(unit.getHead(), StatsType.HEAD));
		}
		if(StringUtils.isNotBlank(unit.getArmor())) {
			statList.add(this.getItemStats(unit.getArmor(), StatsType.ARMOR));
		}
		if(StringUtils.isNotBlank(unit.getAccessory())) {
			statList.add(this.getItemStats(unit.getAccessory(), StatsType.ACCESSORY));
		}
		
		UnitStats unitStats = new UnitStats();
		for(StatsData statData: statList) {
			if(statData.getStatsData() != null) {
				unitStats.addStats(statData.getStatsData());
			} else {
				String message = MessageFormat.format("Missing stat from mustadio service data for player {0} of class {1} with item {2} and stat type {3}",
						event.getPlayer(), unit.getClassName(), statData.getStatsName(), StringUtils.capitalize(statData.getStatsType().toString()));
				log.warn(message);
				this.noisyWebhookManager.sendMessage(message);
			}
		}
		return unitStats;
	}

	@Override
	public void refreshMustadioData() {
		log.info("refreshing mustadio data");
		try {
			this.itemStatsMap = this.mustadioRestService.fetchMustadioItemsData().getItems().stream()
					.collect(Collectors.toMap(item -> StringUtils.lowerCase(item.getName()), Item::getStats));
			this.classBaseStats = this.mustadioRestService.fetchMustadioClassData().getClasses().stream()
					.collect(Collectors.toMap(ClassDataKey::new, ClassData::getBaseStats));
		} catch (MustadioApiException e) {
			log.error("Error calling mustadio service", e);
			this.errorWebhookManager.sendException(e, "Error calling mustadio service.");
		}
		log.info("mustadio refresh complete");
	}
	
	private StatsData getBaseClassStats(Unit unit) {
		ClassDataKey key = new ClassDataKey(unit);
		Stats statResult = this.classBaseStats.get(key);
		StatsData data = new StatsData(StatsType.BASE_CLASS, key.toString(), statResult);
		return data;
	}
	
	private StatsData getItemStats(String key, StatsType statsType) {
		String cleanedKey = StringUtils.lowerCase(key);
		Stats stats = this.itemStatsMap.get(cleanedKey);
		StatsData data = new StatsData(statsType, cleanedKey.toString(), stats);
		return data;
	}

}

@Data
@NoArgsConstructor
class ClassDataKey {
	Gender gender;
	String className;
	
	public ClassDataKey(ClassData classData) {
		this.gender = classData.getGender();
		this.className = StringUtils.lowerCase(classData.getName());
	}
	
	public ClassDataKey(Unit unit) {
		this.gender = unit.getGender();
		this.className = StringUtils.replace(StringUtils.lowerCase(unit.getClassName()), " ", ""); //mustadio removes spaces from class names
	}
}

enum StatsType {
	BASE_CLASS,
	MAIN_HAND,
	OFF_HAND,
	HEAD,
	ARMOR,
	ACCESSORY;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class StatsData {
	private StatsType statsType;
	private String statsName;
	private Stats statsData;
}
