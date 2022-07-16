package fft_battleground.repo.model;

import java.sql.Timestamp;
import java.time.Instant;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "music_listen_count")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class MusicListenCount {
	@Id
	@Column(name = "song_id", nullable = false)
	private Long songId;
	
	@Column(name = "song", nullable = false)
	private String song;
	
	@Column(name = "occurences", nullable = false)
	@ColumnDefault("0")
	private long occurences = 0;
	
    @CreationTimestamp
    @JsonIgnore
    private Timestamp createDateTime;
 
    @UpdateTimestamp
    @JsonIgnore
    private Timestamp updateDateTime;
	
    public MusicListenCount() {}
    
    public MusicListenCount(Long songId, String song) {
    	this.songId = songId;
    	this.song = song;
    	this.occurences = 1;
    	this.createDateTime = Timestamp.from(Instant.now());
    	this.updateDateTime = Timestamp.from(Instant.now());
    }
    
}
