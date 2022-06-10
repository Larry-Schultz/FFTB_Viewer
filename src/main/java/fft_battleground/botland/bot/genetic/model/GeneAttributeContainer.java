package fft_battleground.botland.bot.genetic.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneAttributeContainer {
	private List<GeneAttributePair> attributes;
}
