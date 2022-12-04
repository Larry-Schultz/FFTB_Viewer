package fft_battleground.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import fft_battleground.image.model.Images;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageCacheServiceImpl implements ImageCacheService {
	
	@Autowired
	private Images images;
	
	@Cacheable("characterImages")
	@Override
	public byte[] getCharacterImage(String characterName) {
		String basePath = "/static";
		String imagePath = images.getCharacterImagePath(characterName);
		String path = basePath + imagePath;
		byte[] data = this.loadFileData(path);
		
		return data;
	}
	
	@Override
	public byte[] justGetMeACharacterImage(String characterName) {
		Collection<String> characterNames = this.images.getCharacters().keySet();
		String closestMatch = this.getClosestMatch(characterName, characterNames);
		byte[] image = this.getCharacterImage(closestMatch);
		return image;
	}
	
	@Cacheable("portraitImages")
	@Override
	public byte[] getPortaitImage(String characterName) {
		String basePath = "/static/img/portraits";
		String imagePath = "/" + characterName;
		String path = basePath + imagePath + ".gif";
		byte[] data = this.loadFileData(path);
		
		return data;
	}
	
	protected byte[] loadFileData(String path) {
		Resource imageResource = new ClassPathResource(path);
		InputStream in = null;
		byte[] result = null;
		try {
			in = imageResource.getInputStream();
			result = IOUtils.toByteArray(in);
		} catch (IOException e) {
			log.info("Could not load file with path {}", path);
			result = null;
		}
		
		return result;
	}
	
	private String getClosestMatch(String searchCriteria, Collection<String> names) {
		LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();
		String closestMatch = names.stream().map(name -> Pair.of(name, distanceCalculator.apply(name, searchCriteria)))
				.sorted(Comparator.comparing(Pair::getRight))
				.findFirst().get().getLeft();
		
		return closestMatch;
	}
}
