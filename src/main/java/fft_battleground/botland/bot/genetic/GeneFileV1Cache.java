package fft_battleground.botland.bot.genetic;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import fft_battleground.botland.bot.genetic.model.ResultData;
import fft_battleground.exception.BotConfigException;

@Component
public class GeneFileV1Cache extends GeneFileCache<ResultData> {

	public GeneFileV1Cache() {
		super();
	}
	
	@Override
	protected Class<ResultData> getCacheType() {
		return ResultData.class;
	}
	
	@Cacheable("genefileV1")
	@Override
	public ResultData getGeneData(String filename) throws BotConfigException {
		ResultData data = super.getGeneData(filename);
		return data;
	}
	
	@Override
	protected String baseFolder() {
		return "GeneFiles/GeneTrainerV1";
	}
	
	@Override
	public ResultData getLatestFile() throws BotConfigException {
		ResultData data = super.getLatestFile();
		return data;
	}

}
