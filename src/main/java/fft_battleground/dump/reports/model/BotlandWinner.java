package fft_battleground.dump.reports.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotlandWinner {
	private String dateString;
	private List<String> winners;
	private Integer balance;
}
