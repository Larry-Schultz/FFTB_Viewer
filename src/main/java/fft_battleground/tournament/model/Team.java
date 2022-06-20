package fft_battleground.tournament.model;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {
	@JsonProperty("Player")
	@JsonDeserialize(using = PlayerFieldBattlegroundTeamDeserializer.class)
	@JsonSerialize(using = PlayerFieldBattlegroundTeamSerializer.class)
	private BattleGroundTeam team;
	@JsonProperty("Name")
	private String Name;
	@JsonProperty("Palettes")
	@JsonDeserialize(using = PalettesFieldDeserializer.class)
	@JsonSerialize(using = PalettesFieldSerializer.class)
	private Palette Palettes;
	@JsonProperty("Units")
	private List<Unit> Units;
	
}

class PlayerFieldBattlegroundTeamDeserializer extends StdDeserializer<BattleGroundTeam> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3962359949792620878L;

	public PlayerFieldBattlegroundTeamDeserializer() {
		super(BattleGroundTeam.class);
	}
	
	protected PlayerFieldBattlegroundTeamDeserializer(Class<?> vc) {
		super(vc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BattleGroundTeam deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String json = p.getText();
		
		BattleGroundTeam team = null;
		if(StringUtils.startsWith(json, "!z")) {
			String cleanedString = StringUtils.replace(json, "!z", "");
			team = BattleGroundTeam.parse(cleanedString);
		} else if(StringUtils.startsWith(json, "!")) {
			String cleanedString = StringUtils.replace(json, "!", "");
			team = BattleGroundTeam.parse(cleanedString);
		}
		
		return team;
	}

}

class PlayerFieldBattlegroundTeamSerializer extends StdSerializer<BattleGroundTeam> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7672752181097052631L;

	public PlayerFieldBattlegroundTeamSerializer() {
		super(BattleGroundTeam.class);
	}
	
	protected PlayerFieldBattlegroundTeamSerializer(Class<BattleGroundTeam> t) {
		super(t);
	}

	@Override
	public void serialize(BattleGroundTeam value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		String result = null;
		if(value == BattleGroundTeam.CHAMPION) {
			result = "!zChamp";
		} else {
			result = "!" + value.getProperName();
		}
		gen.writeString(result);
		
	}

}

class PalettesFieldDeserializer extends StdDeserializer<Palette> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3962359949792620878L;
	
	public PalettesFieldDeserializer() {
		super(Palette.class);
	}

	protected PalettesFieldDeserializer(Class<?> vc) {
		super(vc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Palette deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		String json = p.getText();
		
		Palette palettes = null;
		
		String[] paletteStrings = StringUtils.split(json, "/");
		if(paletteStrings.length == 2) {
			BattleGroundTeam palette1 = BattleGroundTeam.parse(paletteStrings[0]);
			BattleGroundTeam palette2 = BattleGroundTeam.parse(paletteStrings[1]);
			palettes = new Palette(palette1, palette2);
		}
		
		return palettes;
	}

}

class PalettesFieldSerializer extends StdSerializer<Palette> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7672752181097052631L;

	public PalettesFieldSerializer() {
		super(Palette.class);
	}
	
	protected PalettesFieldSerializer(Class<Palette> t) {
		super(t);
	}

	@Override
	public void serialize(Palette value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		String result = null;
		if(value != null && value.getPrimary() != null && value.getSecondary() != null) {
			result = value.getPrimary().getProperName() + "/" + value.getSecondary().getProperName();
		}
		gen.writeString(result);
		
	}

}

