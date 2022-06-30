package fft_battleground.util.jackson;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import fft_battleground.model.Gender;

public class GenderDeserializer extends StdDeserializer<Gender> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4574143432952417450L;

	public GenderDeserializer() {
		super(Gender.class);
	}
	
	protected GenderDeserializer(Class<?> vc) {
		super(vc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Gender deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String json = StringUtils.lowerCase(p.getText());
		Gender gender = Gender.getGenderFromString(json);
		return gender;
	}

}