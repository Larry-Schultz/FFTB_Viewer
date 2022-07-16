package fft_battleground.botland.bot.genetic;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import fft_battleground.botland.bot.genetic.model.GeneTrainerV2BotData;
import fft_battleground.exception.BotConfigException;

@Component
public class GeneFileV2Cache extends GeneFileCache<GeneTrainerV2BotData> {

	public GeneFileV2Cache() {
		super();
	}
	
	@Override
	protected Class<GeneTrainerV2BotData> getCacheType() {
		return GeneTrainerV2BotData.class;
	}
	
	
	@Override
	public GeneTrainerV2BotData getGeneData(String filename) throws BotConfigException {
		GeneFile<GeneTrainerV2BotData> geneFile = this.loadGeneDataFromFile(filename);
		
		GeneTrainerV2BotData data = (GeneTrainerV2BotData) geneFile.getData();
		if(this.genefileWithMostRecentData == null || this.genefileWithMostRecentData.getLeft().before(geneFile.getFileDate())) {
			this.genefileWithMostRecentData = Pair.of(geneFile.getFileDate(), geneFile.getFilename());
		}
		
		return data;
	}
	
	@Override
	protected String baseFolder() {
		return "GeneFiles/GeneTrainerV2";
	}
	
	@Override
	@Cacheable("genefileV2")
	public GeneFile<GeneTrainerV2BotData> loadGeneDataFromFile(String filename) throws BotConfigException {
		GeneFile<GeneTrainerV2BotData> botData = super.loadGeneDataFromFile(filename);
		botData.getData().init();
		return botData;
	}

}
