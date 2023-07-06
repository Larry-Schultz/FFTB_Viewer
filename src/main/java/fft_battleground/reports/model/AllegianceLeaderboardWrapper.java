package fft_battleground.reports.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@Data
public class AllegianceLeaderboardWrapper {
	@JsonIgnore
	private static final String generationDateFormatString = "yyyy-MM-dd hh:mm:ss aa zzz";
	
	private List<AllegianceLeaderboard> leaderboards;
	private int gilCap;
	private Date generationDate;
	
	public AllegianceLeaderboardWrapper() {}
	
	public AllegianceLeaderboardWrapper(List<AllegianceLeaderboard> leaderboards) {
		this.leaderboards = leaderboards;
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
