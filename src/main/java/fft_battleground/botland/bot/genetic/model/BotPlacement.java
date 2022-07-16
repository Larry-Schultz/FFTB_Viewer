package fft_battleground.botland.bot.genetic.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotPlacement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5422032040134837934L;
	
	private String botName;
	private long gil;
}
