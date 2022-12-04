package fft_battleground.repo.model;

import java.sql.Timestamp;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "music_listen_count_history")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class MusicListenCountHistory {

	public static final String DISPLAY_FORMAT = "MM-dd-yyyy";
	
    @Id
    @Column(name = "date_string", nullable = false)
    @JsonIgnore
	private String dateString;
    
	@Column(name = "occurences", nullable = false)
	@ColumnDefault("0")
	private long occurences = 0;
	
    @CreationTimestamp
    @JsonIgnore
    private Timestamp createDateTime;
    
    public MusicListenCountHistory() {}
    
	public MusicListenCountHistory(String currentDateString, Long occurences) {
		this.dateString = currentDateString;
		this.occurences = occurences;
	}
}
