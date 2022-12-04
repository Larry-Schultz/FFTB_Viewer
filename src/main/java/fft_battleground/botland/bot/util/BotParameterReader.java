package fft_battleground.botland.bot.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import fft_battleground.botland.bot.exception.BotConfigException;
import fft_battleground.botland.model.BotParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotParameterReader {
	private Map<String, BotParam> map;
	
	public Optional<String> readStringParam(String parameter) {
		Optional<String> result = Optional.empty();
		if(this.map.containsKey(parameter)) {
			String mapData = this.readResultFromMap(parameter);
			result = Optional.of(mapData);
		}
		return result;
	}
	
	public Optional<Boolean> readBooleanParam(String parameter) {
		Optional<String> stringResult = this.readStringParam(parameter);
		Optional<Boolean> result = stringResult.isPresent() ?  this.convertToBoolean(stringResult) : Optional.empty();
		return result;
	}
	
	public <T> Optional<T> readParam(String parameter, Function<String, T> conversionFunction) {
		Optional<String> stringResult = this.readStringParam(parameter);
		Optional<T> result = Optional.empty();
		try {
			result = stringResult.isPresent() ?  Optional.of(conversionFunction.apply(stringResult.get())) : Optional.empty();
		} catch(NullPointerException e) {
			result = Optional.empty();
		}
		return result;
	}
	
	public String readRequiredStringParam(String parameter, String errorMessageIfMissing) throws BotConfigException {
		String result = this.readStringParam(parameter).orElseThrow(() -> new BotConfigException(errorMessageIfMissing));
		return result;
	}
	
	public boolean readOptionalBooleanParam(String parameter) {
		Optional<Boolean> booleanOptional = this.readBooleanParam(parameter);
		boolean result = booleanOptional.isPresent() ? booleanOptional.get() : false;
		return result;
	}
	
	public static Supplier<BotConfigException> throwBotconfigException(String message) {
		return () -> new BotConfigException(message);
	}
	
	protected Optional<Boolean> convertToBoolean(Optional<String> option) {
		Optional<Boolean> result = Optional.of(Boolean.valueOf(option.get()));
		return result;
	}
	
	protected String readResultFromMap(String parameter) {
		String result = this.map.get(parameter).getValue();
		return result;
	}
}
