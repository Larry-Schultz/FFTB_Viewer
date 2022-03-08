package fft_battleground.repo.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.util.BotReceivedBets;
import fft_battleground.util.BotReceivedBetsSerializer;
import fft_battleground.util.GenericPairing;
import fft_battleground.util.hibernate.BattleGroundTeamConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "bot_bet_data", indexes= {
		@Index(columnList ="tournament_id,left_team,right_team", name = "tournament_teams_idx")})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
public class BotBetData {
	
	@Id
	@SequenceGenerator(name="bot_bet_data_generator", sequenceName = "bot_bet_data_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bot_bet_data_generator")
    @Column(name = "bot_bet_data_id", nullable = false)
	private Long botBetDataId;
	
	@Column(name = "tournament_id", nullable = false)
	private Long tournamentId;
	
    @Convert(converter = BattleGroundTeamConverter.class)
    @Column(name="left_team", nullable = true)
	private BattleGroundTeam leftTeam;
	
    @Convert(converter = BattleGroundTeamConverter.class)
    @Column(name="right_team", nullable = true)
	private BattleGroundTeam rightTeam;

	@Column(name = "left_side_pot", nullable = false)
	private Integer leftSidePot;
	
	@Column(name = "right_side_pot", nullable = false)
	private Integer rightSidePot;
	
	@Lob
	@Convert(converter = BotReceivedBetsString.class)
	@JsonSerialize(using = BotReceivedBetsSerializer.class)
	@Column(name = "left_bets", columnDefinition="BLOB")
	private BotReceivedBets leftBets;
	
	@Lob
	@Convert(converter = BotReceivedBetsString.class)
	@JsonSerialize(using = BotReceivedBetsSerializer.class)
	@Column(name = "right_bets", columnDefinition="BLOB")
	private BotReceivedBets rightBets;
	
	@CreationTimestamp
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    private Date createDateTime;
    
    public BotBetData() {
    	
    }
    
	public BotBetData(Long id, BattleGroundTeam leftTeam, BattleGroundTeam rightTeam, Integer leftSideTotal,
			Integer rightSideTotal, Map<String, Integer> leftBetsMap, Map<String, Integer> rightBetsMap) {
		this.tournamentId = id;
		this.leftTeam = leftTeam;
		this.rightTeam = rightTeam;
		this.leftSidePot = leftSideTotal;
		this.rightSidePot = rightSideTotal;
		this.leftBets = new BotReceivedBets(leftBetsMap);
		this.rightBets = new BotReceivedBets(rightBetsMap);
	}

}

@Slf4j
class BotReceivedBetsString implements AttributeConverter<BotReceivedBets, String> {

	@Override
	public String convertToDatabaseColumn(BotReceivedBets attribute) {
		List<GenericPairing<String, Integer>> pairings = GenericPairing.convertMapToGenericPairList(attribute.getBets());
		ObjectMapper jsonMapper = new ObjectMapper();
		String json = null;
		try {
			json = jsonMapper.writeValueAsString(new MapStringIntegerJsonDataObject(pairings));
		} catch (JsonProcessingException e) {
			log.error("Error parsing map data", e);
		}
		
		return json;
	}

	@Override
	public BotReceivedBets convertToEntityAttribute(String dbData) {
		ObjectMapper jsonMapper = new ObjectMapper();
		BotReceivedBets data = new BotReceivedBets(Collections.emptyMap());
		String json = dbData;
		try {
			if(!StringUtils.startsWith(json, "{\"data\":")) {
				json = "{\"data\":" + json + " } ";
			}
			MapStringIntegerJsonDataObject pairings = jsonMapper.readValue(json, MapStringIntegerJsonDataObject.class);
			data = new BotReceivedBets(pairings.getData().stream().collect(Collectors.toMap(GenericPairing<String, Integer>::getLeft, GenericPairing<String, Integer>::getRight)));
		} catch (JsonProcessingException e) {
			log.error("Error parsing map data", e);
		}
		
		return data;
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class MapStringIntegerJsonDataObject {
	private List<GenericPairing<String, Integer>> data;
}
