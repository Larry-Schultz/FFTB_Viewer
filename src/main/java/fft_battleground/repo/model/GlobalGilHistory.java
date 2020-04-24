package fft_battleground.repo.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Table(name = "global_gil_history")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
public class GlobalGilHistory {

	public static final String dateFormatString = "dd-MM-yyyy";
	
    @Id
    @Column(name = "date_string", nullable = false)
	private String date_string;
	
    @Column(name = "player_count", nullable = false)
	private Integer player_count;
	
    @Column(name = "global_gil_count", nullable = false)
	private Long global_gil_count;
    
    @JsonIgnore
    @CreationTimestamp
    private Date createDateTime;
    
    @JsonIgnore
    @UpdateTimestamp
    private Date updateDateTime;
    
    public GlobalGilHistory() {}
    
	public GlobalGilHistory(String currentDateString, Long globalGilCount, Integer globalPlayerCount) {
		this.date_string = currentDateString;
		this.player_count = globalPlayerCount;
		this.global_gil_count = globalGilCount;
	}
}
