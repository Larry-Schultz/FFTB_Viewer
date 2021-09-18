package fft_battleground.dump.reports.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotlandWinner {
	private String dateString;
	private String winners;
	private Integer balance;
}
