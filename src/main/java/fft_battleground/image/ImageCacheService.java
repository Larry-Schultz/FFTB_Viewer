package fft_battleground.image;

public interface ImageCacheService {
	byte[] getCharacterImage(String characterName);
	byte[] justGetMeACharacterImage(String characterName);
	byte[] getPortaitImage(String characterName);
}
