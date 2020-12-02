package fft_battleground.dump.reports.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
@AllArgsConstructor
public class PlayerLeaderboard {
	@JsonIgnore
	private static final String generationDateFormatString = "yyyy-MM-dd hh:mm:ss aa zzz";
	
	private List<LeaderboardData> highestPlayers;
	private List<LeaderboardData> topPlayers;
	private Date generationDate;
	
	public PlayerLeaderboard() {}
	
	public PlayerLeaderboard(List<LeaderboardData> highestPlayers, List<LeaderboardData> topPlayers) {
		this.highestPlayers = highestPlayers;
		this.topPlayers = topPlayers;
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