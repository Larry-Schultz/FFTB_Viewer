package fft_battleground.botland.bot.genetic.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.botland.bot.exception.BotConfigException;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class MapGeneAttributes 
extends GeneAttributes
implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4805458165145763116L;
	
	@JsonIgnore
	private transient Map<MapKey, Double> mapGeneMap;
	
	@Override
	public void init() throws BotConfigException {
		this.mapGeneMap = new HashMap<>();
		for(GeneAttributePair geneAttributePair: this.geneAttributes.getAttributes()) {
			this.mapGeneMap.put(new MapKey(geneAttributePair), geneAttributePair.getValue());
		}
	}
	
	public Optional<Double> mapGene(BattleGroundTeam side, int mapNumber) {
		MapKey key = new MapKey(side, mapNumber);
		Double mapGene = this.mapGeneMap.get(key);
		Optional<Double> result = mapGene != null ? Optional.of(mapGene) : Optional.empty();
		return result;
	}
}

@Data
@NoArgsConstructor
class MapKey {
	private static final String MAP_NAME_SPLIT_CHARACTER = "-";
	private MapSide side;
	private int mapNumber;
	
	public MapKey(GeneAttributePair geneAttributePair) throws BotConfigException {
		String[] dashSplit = StringUtils.split(geneAttributePair.getKey(), MAP_NAME_SPLIT_CHARACTER);
		if(dashSplit.length != 2) {
			throw new BotConfigException("Map Gene Attribute for GeneTrainerV2 is missing map data");
		}
		
		String mapNumberString = dashSplit[0];
		String sideString = dashSplit[1];
		BattleGroundTeam sideTeam = BattleGroundTeam.parse(sideString);
		
		this.mapNumber = Integer.valueOf(mapNumberString);
		this.side = MapSide.getMapSide(sideTeam);
	}
	
	public MapKey(BattleGroundTeam side, int mapNumber) {
		this.side = MapSide.getMapSide(side);
		this.mapNumber = mapNumber;
	}
	
}

enum MapSide {
	LEFT("Left", BattleGroundTeam.LEFT),
	RIGHT("Right", BattleGroundTeam.RIGHT);
	
	MapSide(String name, BattleGroundTeam side) {
		this.name = name;
		this.side = side;
	}
	
	private String name;
	private BattleGroundTeam side;
	
	public static MapSide getMapSide(BattleGroundTeam side) throws NoSuchElementException {
		return Stream.of(MapSide.values()).filter(mapSide -> mapSide.getSide() == side).findFirst().orElseThrow();
	}
	
	public BattleGroundTeam getSide() {
		return this.side;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
