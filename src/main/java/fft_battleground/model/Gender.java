package fft_battleground.model;

import org.apache.commons.lang3.StringUtils;

public enum Gender {
	MALE("male"),
	FEMALE("female"),
	MONSTER("monster");
	
	private String genderString;
	
	private Gender(String genderString) {
		this.genderString = genderString;
	}
	
	public String getGenderString() {
		return this.genderString;
	}
	
	public static boolean isGender(String possibleGenderString) {
		for(Gender gender : Gender.values()) {
			if(StringUtils.equalsIgnoreCase(gender.getGenderString(), possibleGenderString)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static Gender getGenderFromString(String possibleGenderString) {
		for(Gender gender : Gender.values()) {
			if(StringUtils.equalsIgnoreCase(gender.getGenderString(), possibleGenderString)) {
				return gender;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return this.genderString;
	}
}