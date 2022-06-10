package fft_battleground.tournament.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentInfo implements Comparable<TournamentInfo> {
	@JsonProperty("ID")
	private Long ID;
	@JsonProperty("LastMod")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ssXXX")
	private Date LastMod;
	@JsonProperty("Maps")
	private List<String> Maps;
	@JsonProperty("Winners")
	private List<String> Winners;
	@JsonProperty("SkillDrop")
	private String SkillDrop;
	@JsonProperty("Complete")
	private Boolean Complete;
	
	public TournamentInfo() {}

	@Override
	public int compareTo(TournamentInfo o) {
		return this.ID.compareTo(o.getID());
	}
}
