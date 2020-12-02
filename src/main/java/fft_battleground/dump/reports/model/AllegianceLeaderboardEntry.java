package fft_battleground.dump.reports.model;

import java.text.NumberFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class AllegianceLeaderboardEntry  implements Comparable<AllegianceLeaderboardEntry> {
	private String name;
	private Integer position;
	private Integer balance;
	
	public AllegianceLeaderboardEntry() {}
	
	public AllegianceLeaderboardEntry(String name, Integer balance) {
		this.name = name;
		this.balance = balance;
	}
	
	@JsonIgnore
	public String getFormattedPosition() {
		Integer position = this.position + 1;
		String result = position.toString();
		return result;
	}
	
	@JsonIgnore
	public String getFormattedBalance() {
		NumberFormat myFormat  = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		String result = myFormat.format(this.balance);
		return result;
	}

	@Override
	public int compareTo(AllegianceLeaderboardEntry arg0) {
		return this.balance.compareTo(arg0.getBalance());
	}
}