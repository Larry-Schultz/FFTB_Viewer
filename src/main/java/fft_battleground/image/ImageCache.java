package fft_battleground.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ImageCache {
	
	@Autowired
	private Images images;
	
	private Cache<String, byte[]> cache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();
	
	public byte[] getCharacterImage(String characterName) {
		String basePath = "/static";
		String imagePath = images.getCharacterImagePath(characterName);
		String path = basePath + imagePath;
		byte[] data = cache.getIfPresent(path);
		if(data == null) {
			data = this.loadFileData(path);
		}
		
		return data;
	}
	
	public byte[] getPortaitImage(String characterName) {
		String basePath = "/static/img/portraits";
		String imagePath = "/" + characterName;
		String path = basePath + imagePath;
		byte[] data = cache.getIfPresent(path);
		if(data == null) {
			data = this.loadFileData(path);
		}
		
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
			log.error("Could not load file with path {}", path, e);
			result = null;
		}
		
		return result;
	}
}
