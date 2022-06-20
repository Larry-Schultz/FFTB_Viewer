package fft_battleground.image;

public interface ImageCacheService {
	byte[] getCharacterImage(String characterName);
	public byte[] getPortaitImage(String characterName);
}
