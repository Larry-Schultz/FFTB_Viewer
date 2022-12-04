package fft_battleground.botland.bot.util;

import fft_battleground.botland.bot.exception.BotConfigException;

public interface BotUsesGeneFile {
	public static final String GENE_FILE_PARAMETER = "geneFile";
	public static final String MISSING_GENE_FILE_PARAMETER_ERROR_MESSAGE = "Missing required parameter " + GENE_FILE_PARAMETER;
	
	public default String getGeneFileParameter() {
		return GENE_FILE_PARAMETER;
	}
	
	public default String readGeneFileParameter(BotParameterReader reader) throws BotConfigException {
		String result = reader.readRequiredStringParam(getGeneFileParameter(), MISSING_GENE_FILE_PARAMETER_ERROR_MESSAGE);
		return result;
	}
}
