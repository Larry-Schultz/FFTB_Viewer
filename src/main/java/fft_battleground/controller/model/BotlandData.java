package fft_battleground.controller.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fft_battleground.botland.model.BotData;
import fft_battleground.repo.model.BotHourlyData;
import fft_battleground.repo.model.Bots;
import fft_battleground.util.GenericElementOrdering;
import fft_battleground.util.GenericPairing;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class BotlandData {
	
	private List<Bots> botData;
	private String primaryBotAccountName;
	private String primaryBotName;
	private List<BotlandConfigData> botConfigData;
	private List<BotlandHourlyData> botHourlyDataMap;
	private List<GenericPairing<String, String>> lastBotPersonalityResponses;
	
	public BotlandData(List<Bots> botData, String primaryBotAccountName, String primaryBotName,
			Map<String, BotData> botConfigData, Map<String, List<GenericElementOrdering<BotHourlyData>>> botHourlyDataMap,
			Map<String, String> lastBotPersonalityResponses) {
		this.botData = botData;
		this.primaryBotAccountName = primaryBotAccountName;
		this.primaryBotName = primaryBotName;
		this.botConfigData = BotlandConfigData.convertToBotlandConfigDataType(botConfigData);
		this.botHourlyDataMap = BotlandHourlyData.convertToBotlandHourlyDataType(botHourlyDataMap);
		this.lastBotPersonalityResponses = GenericPairing.convertMapToGenericPairList(lastBotPersonalityResponses);
	}
}

@Data
@AllArgsConstructor
class BotlandHourlyData {
	public String botName;
	public List<GenericElementOrdering<BotHourlyData>> botHourlyData;
	
	public static List<BotlandHourlyData> convertToBotlandHourlyDataType(Map<String, List<GenericElementOrdering<BotHourlyData>>> botHourlyDataMap) {
		List<BotlandHourlyData> botlandHourlyDataList = botHourlyDataMap.keySet().stream().map(botName -> new BotlandHourlyData(botName, botHourlyDataMap.get(botName))).collect(Collectors.toList());
		return botlandHourlyDataList;
	}
}
