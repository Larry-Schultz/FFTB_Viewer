package fft_battleground.botland.personality.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PersonalityMatrixEntry {
	private Integer beginInclusive;
	private Integer endInclusive;
	private Function<Map<PersonalityDisplayFields, String>, String> messageFunction;
	private boolean display;
	private List<PersonalityDisplayFields> requiredFields;
	
	public PersonalityMatrixEntry(int begin, int end, String message, boolean display) {
		this.beginInclusive = begin;
		this.endInclusive = end;
		this.messageFunction = map -> message;
		this.display = display;
		this.requiredFields = Collections.emptyList();
	}
	
	public PersonalityMatrixEntry(int begin, int end, Function<Map<PersonalityDisplayFields, String>, String> messageFunction, boolean display, PersonalityDisplayFields[] requiredFields) {
		this.beginInclusive = begin;
		this.endInclusive = end;
		this.messageFunction = messageFunction;
		this.display = display;
		this.requiredFields = Arrays.asList(requiredFields);
	}
	
	public String getMessage(Map<PersonalityDisplayFields, String> fieldData) {
		String message = this.messageFunction.apply(fieldData);
		return message;
	}
}