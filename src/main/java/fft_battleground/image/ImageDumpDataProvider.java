package fft_battleground.image;

import java.util.List;
import fft_battleground.exception.DumpException;
import fft_battleground.image.model.DumpActiveMap;

public interface ImageDumpDataProvider {

	List<String> getActivePortraits() throws DumpException;
	List<DumpActiveMap> getActiveMaps() throws DumpException;
}
