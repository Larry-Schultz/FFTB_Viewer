package fft_battleground.botland.bot.genetic;

import fft_battleground.botland.bot.genetic.model.GeneTrainerV2BotData;
import fft_battleground.exception.BotConfigException;

public class GeneFileV2Cache extends GeneFileCache<GeneTrainerV2BotData> {

	public GeneFileV2Cache() {
		super();
	}
	
	public GeneFileV2Cache(Long cacheDuration) {
		super(cacheDuration);
	}
	
	@Override
	protected Class<GeneTrainerV2BotData> getCacheType() {
		return GeneTrainerV2BotData.class;
	}
	
	@Override
	protected String baseFolder() {
		return "GeneFiles/GeneTrainerV2";
	}
	
	@Override
	protected GeneFile<GeneTrainerV2BotData> loadGeneDataFromFile(String filename) throws BotConfigException {
		GeneFile<GeneTrainerV2BotData> botData = super.loadGeneDataFromFile(filename);
		botData.getData().init();
		return botData;
	}

}
