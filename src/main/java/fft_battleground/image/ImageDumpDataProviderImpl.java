package fft_battleground.image;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import fft_battleground.dump.data.AbstractDataProvider;
import fft_battleground.dump.data.DumpResourceManager;
import fft_battleground.dump.model.FolderListData;
import fft_battleground.exception.DumpException;
import fft_battleground.image.model.DumpActiveMap;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageDumpDataProviderImpl 
extends AbstractDataProvider 
implements ImageDumpDataProvider {
	private static final String GFX_URL = "http://www.fftbattleground.com/fftbg/gfx/";
	private static final String MAPS_URL = "http://www.fftbattleground.com/fftbg/Maps.txt";

	public ImageDumpDataProviderImpl(@Autowired DumpResourceManager dumpResourceManager) {
		super(dumpResourceManager);
	}

	@Override
	@Cacheable("activePortraits")
	public List<String> getActivePortraits() throws DumpException {
		Set<FolderListData> activePortraitFolderListData = this.getFolderDataFromList(GFX_URL, "gif");
		List<String> activePortraits = activePortraitFolderListData.stream().map(FolderListData::getEntityName)
				.sorted().collect(Collectors.toList());
		return activePortraits;
	}

	@Override
	@Cacheable("activeMaps")
	public List<DumpActiveMap> getActiveMaps() throws DumpException {
		List<DumpActiveMap> activeMaps = new ArrayList<>();
		Function<String, DumpActiveMap> activeMapLineParser = (mapStr) -> {
			String mapNumberString = StringUtils.substringBefore(mapStr, ")");
			Integer mapNumber = Integer.valueOf(mapNumberString);
			String mapName = StringUtils.trim(StringUtils.substringAfter(mapStr, ")"));
			DumpActiveMap activeMap = new DumpActiveMap(mapNumber, mapName);
			return activeMap;
			
		};
		this.getDataFromMultilineFile(MAPS_URL, activeMaps, activeMapLineParser);
		
		return activeMaps;
	}
}
