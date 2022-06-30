package fft_battleground.repo.model;

import java.sql.Timestamp;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.event.model.BattleGroundEventType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "detector_event_audit")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class DetectorEventAudit {
	
	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "event", nullable = false)
	private BattleGroundEventType event;
	
	@Column(name = "occurences", nullable = false)
	@ColumnDefault("0")
	private long occurences;
	
    @CreationTimestamp
    @JsonIgnore
    private Timestamp createDateTime;
 
    @UpdateTimestamp
    @JsonIgnore
    private Timestamp updateDateTime;
    
    public DetectorEventAudit() {}

	public DetectorEventAudit(BattleGroundEventType type, Integer occurence) {
		this.event = type;
		this.occurences = occurence.longValue();
	}
}
