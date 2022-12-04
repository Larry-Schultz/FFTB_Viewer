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
    
    public MusicListenCount(String song) {
    	this.song = song;
    	this.occurences = 0;
    	this.createDateTime = Timestamp.from(Instant.now());
    	this.updateDateTime = Timestamp.from(Instant.now());
    }
    
    public MusicListenCount(String song, long occurences) {
    	this.song = song;
    	this.occurences = occurences;
    	this.createDateTime = Timestamp.from(Instant.now());
    	this.updateDateTime = Timestamp.from(Instant.now());
    }
    
}
