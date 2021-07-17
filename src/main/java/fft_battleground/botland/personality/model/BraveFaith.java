package fft_battleground.botland.personality.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public enum BraveFaith {
	BRAVE,
	FAITH,
	BOTH,
	NONE;
	
	public static BraveFaith parseString(String braveFaithString) {
		BraveFaith result = null;
		for(BraveFaith braveFaith : BraveFaith.values()) {
			if(StringUtils.equalsIgnoreCase(braveFaith.name(), braveFaithString)) {
				result = braveFaith;
				break;
			}
		}
		
		return result;
	}
}