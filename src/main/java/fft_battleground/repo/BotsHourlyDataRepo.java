package fft_battleground.repo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fft_battleground.repo.model.BotHourlyData;

public interface BotsHourlyDataRepo extends JpaRepository<BotHourlyData, String> {

	@Query("SELECT botHourlyData FROM BotHourlyData botHourlyData WHERE botHourlyData.player = :player")
	public List<BotHourlyData> getBotHourlyDataForBot(@Param("player") String player);
	
	@Query("SELECT botHourlyData FROM BotHourlyData botHourlyData WHERE botHourlyData.player = :player AND botHourlyData.hourValue = :hour_value")
	public BotHourlyData getBotHourlyDataForBotAndCurrentTime(@Param("player") String player, @Param("hour_value") Integer hourValue);
	
	public default List<BotHourlyData> getOrderedBotHourlyDataForBot(String bot) {
		int startHour = BotHourlyData.getHourValueForCurrentTime();
		 
		List<BotHourlyData> botHourlyDataForBot = this.getBotHourlyDataForBot(bot);
		Map<Integer, BotHourlyData> hourIntHourlyDataMap = botHourlyDataForBot.parallelStream().collect(Collectors.toMap(BotHourlyData::getHourValue, Function.identity()));
		List<BotHourlyData> orderedData =this.getHourOrdering(startHour).stream().map(hourInt -> hourIntHourlyDataMap.get(hourInt)).collect(Collectors.toList());
		
		return orderedData;
	}
	
	public default List<Integer> getHourOrdering(int startHour) {
		List<Integer> firstHourList = IntStream.range(startHour, 24).boxed().collect(Collectors.toList());
		List<Integer> secondHourList = IntStream.range(0, startHour).boxed().collect(Collectors.toList());
		
		List<Integer> results = new LinkedList<>();
		results.addAll(firstHourList);
		results.addAll(secondHourList);
		
		return results;
	}
}
