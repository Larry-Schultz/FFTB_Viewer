package fft_battleground.controller.response.model.botland;

import java.util.List;
import java.util.Map;

import fft_battleground.botland.model.BotData;
import fft_battleground.repo.model.BotHourlyData;
import fft_battleground.repo.model.Bots;
import fft_battleground.util.GenericElementOrdering;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotResponseData {
	private String botName;
	private Bots bot;
	private BotData botData;
	private List<GenericElementOrdering<BotHourlyData>> botHourlyDataMap;
	private String lastBotResponse;

}
