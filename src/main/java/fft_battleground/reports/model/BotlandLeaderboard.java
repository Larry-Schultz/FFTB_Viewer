package fft_battleground.reports.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import fft_battleground.repo.model.Bots;
import fft_battleground.util.GenericElementOrdering;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@NoArgsConstructor
public class BotlandLeaderboard {
	private static final int WINNER_LIMIT = 10;
	private List<GenericElementOrdering<BotlandLeaderboardEntry>> botlandEntries;
	private List<GenericElementOrdering<BotlandWinner>> botlandWinners;
	
	@JsonIgnore
	private transient Map<String, BotlandLeaderboardEntry> botLeaderboardEntryMap;
	
	public BotlandLeaderboard(List<Bots> todaysBots, Map<String, BotlandWinner> dateStringWinnersMap, Map<String, Integer> botHighestOfAllTimeMap, 
			Map<String, Long> botWinCountMap, Map<String, Bots> oldestBotEntryMap, Map<String, Long> winCountsPerBot, Map<String, Long> lossCountsPerBot, 
			Map<String, Double> averageBalancePerBot, Map<String, Double> averagePeakBalancePerBotMap, Map<String, Double> averageWinsPerDay, 
			Map<String, Double> averageLossesPerDay, Map<String, Double> averageWinRatePerDay, Map<String, Long> daysParticipatingPerBotMap) {
		this.botlandEntries = this.calculateBotlandEntries(todaysBots, botHighestOfAllTimeMap, botWinCountMap, oldestBotEntryMap, winCountsPerBot, lossCountsPerBot,
				averageBalancePerBot, averagePeakBalancePerBotMap, averageWinsPerDay, averageLossesPerDay, averageWinRatePerDay, daysParticipatingPerBotMap);
		this.botlandWinners = this.calculateBotlandWinners(dateStringWinnersMap);
		this.botLeaderboardEntryMap = this.botlandEntries.stream()
				.map(GenericElementOrdering<BotlandLeaderboardEntry>::getElement)
				.collect(Collectors.toMap(BotlandLeaderboardEntry::getBotName, Function.identity()));
	}
	
	public BotlandLeaderboardEntry getLeaderboardEntryForPlayer(String player) {
		return this.botLeaderboardEntryMap.get(player);
	}
	
	private List<GenericElementOrdering<BotlandLeaderboardEntry>> calculateBotlandEntries(List<Bots> todaysBots, Map<String, Integer> botHighestOfAllTimeMap, 
			Map<String, Long> botWinCountMap, Map<String, Bots> oldestEntriesPerBot, Map<String, Long> winCountsPerBot, Map<String, Long> lossCountsPerBot, 
			Map<String, Double> averageBalancePerBot,  Map<String, Double> averagePeakBalancePerBotMap, Map<String, Double> averageWinsPerDay, 
			Map<String, Double> averageLossesPerDay, Map<String, Double> averageWinRatePerDay, Map<String, Long> daysParticipatingPerBotMap) {
		List<BotlandLeaderboardEntry> entryList = new ArrayList<>();
		for(Bots bot : todaysBots) {
			String botName = bot.getPlayer();
			long endoOfDayVictoryCount = botWinCountMap.get(botName) != null ? botWinCountMap.get(botName) : 0;
			BotlandLeaderboardEntry entry = new BotlandLeaderboardEntry(botName, endoOfDayVictoryCount, daysParticipatingPerBotMap.get(botName), 
					oldestEntriesPerBot.get(botName).getCreateDateTime(), winCountsPerBot.get(botName), lossCountsPerBot.get(botName), 
					botHighestOfAllTimeMap.get(botName), averageBalancePerBot.get(botName),averagePeakBalancePerBotMap.get(botName), 
					averageWinsPerDay.get(botName), averageLossesPerDay.get(botName), averageWinRatePerDay.get(botName));
			entryList.add(entry);
		}
		
		List<GenericElementOrdering<BotlandLeaderboardEntry>> genericOrderedEntryList = GenericElementOrdering.convertToOrderedList(entryList, 
				Comparator.comparing(BotlandLeaderboardEntry::getEndOfDayHighScoreVictoryCount).reversed());
		return genericOrderedEntryList;
	}
	
	private List<GenericElementOrdering<BotlandWinner>> calculateBotlandWinners(Map<String, BotlandWinner> dateStringWinnersMap) {
		List<BotlandWinner> pairedWinnerList = dateStringWinnersMap.values().stream()
				.filter(botlandWinner -> botlandWinner.getWinners().size() < WINNER_LIMIT)
				.collect(Collectors.toList());
		var botlandWinners = GenericElementOrdering.<BotlandWinner>convertToOrderedList(pairedWinnerList, new Comparator<BotlandWinner>() {
			@Override
			@SneakyThrows
			public int compare(BotlandWinner o1, BotlandWinner o2) {
				SimpleDateFormat sdf = Bots.createDateFormatter();
				Date o1Date = sdf.parse(o1.getDateString());
				Date o2Date = sdf.parse(o2.getDateString());
				return o1Date.compareTo(o2Date);
			}
		}.reversed());
		
		return botlandWinners;
	}
	
}
