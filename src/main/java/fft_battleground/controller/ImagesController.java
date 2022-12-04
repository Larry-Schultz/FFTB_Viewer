package fft_battleground.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.exception.DumpException;
import fft_battleground.image.ImageCacheService;
import fft_battleground.image.ImageDumpDataProvider;
import fft_battleground.image.model.DumpActiveMap;
import fft_battleground.util.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping("/images")
@Slf4j
@ApiIgnore
public class ImagesController {
	
	@Autowired
	private ImageCacheService imageCacheService;
	
	@Autowired
	private ImageDumpDataProvider imageDumpDataProvider;
	
	@ApiIgnore
	@GetMapping(value = "/characters/{characterName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody ResponseEntity<byte[]> getImageWithMediaType(@PathVariable("characterName") String characterName) throws IOException {
		String name = characterName;
		if(name == null) {
			return new ResponseEntity<>(new byte[] {}, HttpStatus.BAD_REQUEST);
		} else if(StringUtils.endsWith(name, ".gif")) {
			name = StringUtils.remove(name, ".gif");
		} else if(StringUtils.endsWith(name, ".png")) {
			name = StringUtils.remove(name, ".png");
		}
		byte[] data = this.imageCacheService.getCharacterImage(name);
	    if(data == null) {
	    	return new ResponseEntity<>(new byte[] {}, HttpStatus.NOT_FOUND);
	    } else {
	    	return new ResponseEntity<>(data, HttpStatus.OK);
	    }
	}
	
	@ApiIgnore
	@GetMapping(value = "/portraits/{characterName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody ResponseEntity<byte[]> getPortraitImageWithMediaType(@PathVariable("characterName") String characterName) throws IOException {
		String name = characterName;
		if(name == null) {
			return new ResponseEntity<>(new byte[] {}, HttpStatus.BAD_REQUEST);
		} else if(StringUtils.endsWith(name, ".gif")) {
			name = StringUtils.remove(name, ".gif");
		} else if(StringUtils.endsWith(name, ".png")) {
			name = StringUtils.remove(name, ".png");
		}
		byte[] data = this.imageCacheService.getPortaitImage(name);
		if(data == null) {
			data = this.imageCacheService.justGetMeACharacterImage(name);
		}
	    if(data == null) {
	    	return new ResponseEntity<>(new byte[] {}, HttpStatus.NOT_FOUND);
	    } else {
	    	return new ResponseEntity<>(data, HttpStatus.OK);
	    }
	}
	
	@ApiIgnore
	@GetMapping(value = "/activeMaps", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<GenericResponse<List<DumpActiveMap>>> getActiveMaps() throws DumpException {
		List<DumpActiveMap> activeMaps = this.imageDumpDataProvider.getActiveMaps();
		return GenericResponse.createGenericResponseEntity(activeMaps);
	}
	
	@ApiIgnore
	@GetMapping(value = "/activePortraits", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<GenericResponse<List<String>>> getActivePortraits() throws DumpException {
		List<String> activePortraits = this.imageDumpDataProvider.getActivePortraits();
		return GenericResponse.createGenericResponseEntity(activePortraits);
	}
}
