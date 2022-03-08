package fft_battleground.repo.util;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotReceivedBets {
	private Map<String, Integer> bets;
}