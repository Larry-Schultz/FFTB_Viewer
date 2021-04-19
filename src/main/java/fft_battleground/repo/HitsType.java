package fft_battleground.repo;

import org.apache.commons.lang3.StringUtils;

public enum HitsType {
	USER(),
	CRAWLER,
	NONE,
	BOTH;
	
	@Override
	public String toString() {
		String result = StringUtils.lowerCase(this.name());
		return result;
	}
}
