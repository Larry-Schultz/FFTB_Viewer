package fft_battleground.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.image.ImageCacheService;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping("/images")
@Slf4j
public class ImagesController {
	
	@Autowired
	private ImageCacheService imageCacheService;
	
	@ApiIgnore
	@GetMapping(value = "/characters/{characterName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody ResponseEntity<byte[]> getImageWithMediaType(@PathVariable("characterName") String characterName) throws IOException {
		if(characterName == null) {
			return new ResponseEntity<>(new byte[] {}, HttpStatus.BAD_REQUEST);
		}
		byte[] data = this.imageCacheService.getCharacterImage(characterName);
	    if(data == null) {
	    	return new ResponseEntity<>(new byte[] {}, HttpStatus.NOT_FOUND);
	    } else {
	    	return new ResponseEntity<>(data, HttpStatus.OK);
	    }
	}
	
	@ApiIgnore
	@GetMapping(value = "/portraits/{characterName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody ResponseEntity<byte[]> getPortraitImageWithMediaType(@PathVariable("characterName") String characterName) throws IOException {
		if(characterName == null) {
			return new ResponseEntity<>(new byte[] {}, HttpStatus.BAD_REQUEST);
		}
		byte[] data = this.imageCacheService.getPortaitImage(characterName);
		if(data == null) {
			data = this.imageCacheService.justGetMeACharacterImage(characterName);
		}
	    if(data == null) {
	    	return new ResponseEntity<>(new byte[] {}, HttpStatus.NOT_FOUND);
	    } else {
	    	return new ResponseEntity<>(data, HttpStatus.OK);
	    }
	}
}
