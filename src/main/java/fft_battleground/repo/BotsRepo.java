package fft_battleground.repo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fft_battleground.repo.model.Bots;
import fft_battleground.util.GambleUtil;
import lombok.SneakyThrows;

public interface BotsRepo extends JpaRepository<Bots, Long> {

	@Query("SELECT Bot FROM Bots Bot WHERE Bot.dateString = :dateString AND Bot.player = :name")
	public Bots getBotByDateStringAndName(@Param("dateString") String dateString, @Param("name") String name);
	
	@Query("SELECT Bot FROM Bots Bot WHERE Bot.dateString = :dateString")
	public List<Bots> getBotByDateString(@Param("dateString") String dateString);
	
	public default List<Bots> getBotsForToday() {
		String currentDateString = this.currentDateString();
		List<Bots> botData = this.getBotByDateString(currentDateString);
		
		return botData;
	}
	
	@SneakyThrows
	public default String currentDateString() {
		Date currentDate = new Date();
		SimpleDateFormat sdf = Bots.createDateFormatter();
		String currentDateString = sdf.format(currentDate);
		return currentDateString;
	}
	
	public default Bots addNewBotForToday(String name, boolean isBotAccountSubscriber) {
		String currentDateString = this.currentDateString();
		Bots newBot = new Bots(name, currentDateString, GambleUtil.getMinimumBetForBettor(isBotAccountSubscriber));
		this.saveAndFlush(newBot);
		
		return newBot;
	}
}
