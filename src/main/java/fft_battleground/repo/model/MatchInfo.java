package fft_battleground.repo.model;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import fft_battleground.event.model.MatchInfoEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_info")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class MatchInfo {

    @Id
	@SequenceGenerator(name="match_info_generator", sequenceName = "match_info_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "match_info_generator")
    @Column(name = "match_info_id", nullable = false)
	private Long id;
    
    @Column(name = "map_number", nullable=true)
	private Integer mapNumber;
	
    @Column(name = "map_name", nullable=false)
	private String mapName;
	
    @OneToOne(mappedBy = "matchInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	private Match match;
    
    public MatchInfo() {}
    
    public MatchInfo(MatchInfoEvent event, Match match) {
    	this.mapNumber = event.getMapNumber();
    	this.mapName = event.getMapName();
    	this.match = match;
    }
	
}
