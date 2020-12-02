package fft_battleground.dump.reports.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
public class BotLeaderboard {
	@JsonIgnore
	private static final String generationDateFormatString = "yyyy-MM-dd hh:mm:ss aa zzz";
	
	private Map<String, Integer> botLeaderboard;
	private Date generationDate;
	
	public BotLeaderboard() {}
	
	public BotLeaderboard(Map<String, Integer> botLeaderboard) {
		this.botLeaderboard = botLeaderboard;
		this.generationDate = new Date();
	}
	
	@JsonIgnore
	@SneakyThrows
	public String formattedGenerationDate() {
		SimpleDateFormat sdf = new SimpleDateFormat(generationDateFormatString);
		String result = sdf.format(this.generationDate);
		return result;
	}
}
