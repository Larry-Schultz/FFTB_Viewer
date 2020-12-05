package fft_battleground.repo.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "cache_entries")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class BattleGroundCacheEntry {
	
	@Id
	@Column(name = "cache_entry_id", nullable = false)
	private String cacheEntryId;

	@Lob
	private String cacheData;
	
	@Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_update_date", nullable=true)
	private Date lastUpdateDate;
	
	public BattleGroundCacheEntry() {
		
	}

}
