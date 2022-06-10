package fft_battleground.botland.bot.genetic;

import fft_battleground.botland.bot.genetic.model.ResultData;

public class GeneFileV1Cache extends GeneFileCache<ResultData> {

	public GeneFileV1Cache() {
		super();
	}
	
	public GeneFileV1Cache(Long cacheDuration) {
		super(cacheDuration);
	}
	
	@Override
	protected Class<ResultData> getCacheType() {
		return ResultData.class;
	}
	
	@Override
	protected String baseFolder() {
		return "GeneFiles/GeneTrainerV1";
	}
	
	@Override
	public ResultData getLatestFile() {
		ResultData data = super.getLatestFile();
		return data;
	}

}
