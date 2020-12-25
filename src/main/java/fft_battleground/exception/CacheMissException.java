package fft_battleground.exception;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.repo.BattleGroundCacheEntryKey;

public class CacheMissException extends BattleGroundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1531642895822023574L;

	private BattleGroundCacheEntryKey key;
	
	public CacheMissException(Exception e, BattleGroundCacheEntryKey key) {
		super("cache miss on cache: " + StringUtils.lowerCase(key.toString()), e);
		this.setKey(key);
	}
	
	public CacheMissException(BattleGroundCacheEntryKey key) {
		super("cache miss on cache: " + StringUtils.lowerCase(key.toString()));
		this.setKey(key);
	}

	public BattleGroundCacheEntryKey getKey() {
		return key;
	}

	public void setKey(BattleGroundCacheEntryKey key) {
		this.key = key;
	}

}
