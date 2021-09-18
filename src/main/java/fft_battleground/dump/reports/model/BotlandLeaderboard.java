package fft_battleground.dump.reports.model;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
	private List<GenericElementOrdering<BotlandLeaderboardEntry>> botlandEntries;
	private List<GenericElementOrdering<BotlandWinner>> botlandWinners;
	
	@JsonIgnore
	private transient Map<String, BotlandLeaderboardEntry> botLeaderboardEntryMap;
	
	public BotlandLeaderboard(List<Bots> todaysBots, Map<String, BotlandWinner> dateStringWinnersMap, Map<String, Integer> botHighestOfAllTimeMap, 
			Map<String, Long> botWinCountMap, Map<String, Bots> oldestBotEntryMap, Map<String, Long> winCountsPerBot, Map<String, Long> lossCountsPerBot) {
		this.botlandEntries = this.calculateBotlandEntries(todaysBots, botHighestOfAllTimeMap, botWinCountMap, oldestBotEntryMap, winCountsPerBot, lossCountsPerBot);
		this.botlandWinners = this.calculateBotlandWinners(dateStringWinnersMap);
		this.botLeaderboardEntryMap = this.botlandEntries.stream()
				.map(GenericElementOrdering<BotlandLeaderboardEntry>::getElement)
				.collect(Collectors.toMap(BotlandLeaderboardEntry::getBotName, Function.identity()));
	}
	
	public BotlandLeaderboardEntry getLeaderboardEntryForPlayer(String player) {
		return this.botLeaderboardEntryMap.get(player);
	}
	
	private List<GenericElementOrdering<BotlandLeaderboardEntry>> calculateBotlandEntries(List<Bots> todaysBots, Map<String, Integer> botHighestOfAllTimeMap, 
			Map<String, Long> botWinCountMap, Map<String, Bots> oldestEntriesPerBot, Map<String, Long> winCountsPerBot, Map<String, Long> lossCountsPerBot) {
		List<BotlandLeaderboardEntry> entryList = new ArrayList<>();
		for(Bots bot : todaysBots) {
			String botName = bot.getPlayer();
			long duration = ChronoUnit.DAYS.between(new Timestamp(oldestEntriesPerBot.get(botName).getCreateDateTime().getTime()).toLocalDateTime(), LocalDateTime.now());
			long endoOfDayVictoryCount = botWinCountMap.get(botName) != null ? botWinCountMap.get(botName) : 0;
			BotlandLeaderboardEntry entry = new BotlandLeaderboardEntry(botName, endoOfDayVictoryCount, duration, oldestEntriesPerBot.get(botName).getCreateDateTime(), 
					winCountsPerBot.get(botName), lossCountsPerBot.get(botName), botHighestOfAllTimeMap.get(botName));
			entryList.add(entry);
		}
		
		List<GenericElementOrdering<BotlandLeaderboardEntry>> genericOrderedEntryList = GenericElementOrdering.convertToOrderedList(entryList, 
				Comparator.comparing(BotlandLeaderboardEntry::getEndOfDayHighScoreVictoryCount).reversed());
		return genericOrderedEntryList;
	}
	
	private List<GenericElementOrdering<BotlandWinner>> calculateBotlandWinners(Map<String, BotlandWinner> dateStringWinnersMap) {
		List<BotlandWinner> pairedWinnerList = new ArrayList<>(dateStringWinnersMap.values());
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
