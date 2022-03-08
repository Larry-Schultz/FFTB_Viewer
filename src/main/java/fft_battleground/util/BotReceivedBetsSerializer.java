package fft_battleground.util;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import fft_battleground.repo.util.BotReceivedBets;

@Component
public class BotReceivedBetsSerializer extends StdSerializer<BotReceivedBets> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7672752181097052631L;

	public BotReceivedBetsSerializer() {
		super(BotReceivedBets.class);
	}
	
	protected BotReceivedBetsSerializer(Class<BotReceivedBets> t) {
		super(t);
	}

	@Override
	public void serialize(BotReceivedBets value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		List<GenericPairing<String, Integer>> genericPair = GenericPairing.convertMapToGenericPairList(value.getBets());
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(genericPair);
		gen.writeString(json);
		
	}

}