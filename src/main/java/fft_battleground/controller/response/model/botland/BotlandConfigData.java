package fft_battleground.controller.response.model.botland;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fft_battleground.botland.model.BotData;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BotlandConfigData {
	private String botName;
	private BotData botConfigData;
	
	public static List<BotlandConfigData> convertToBotlandConfigDataType(Map<String, BotData> botConfigData) {
		List<BotlandConfigData> botlandConfigData = botConfigData.keySet().stream().map(botName -> new BotlandConfigData(botName, botConfigData.get(botName))).collect(Collectors.toList());
		return botlandConfigData;
	}
}