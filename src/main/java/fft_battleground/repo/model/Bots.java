package fft_battleground.repo.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.util.GambleUtil;

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "bot_record", indexes= {
		@Index(columnList = "date_string,player", name = "date_string_player_idx")})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class Bots {
	
	public static final String dateFormat = "MM-dd-yyyy";
	
	public Bots() {}
	
	@JsonIgnore
    @Id
	@SequenceGenerator(name="bot_date_key_generator", sequenceName = "bot_date_key_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bot_date_key_generator")
    @Column(name = "bot_entry_id", nullable = false)
	private Long bot_entry_id;
	
	@Column(name="date_string", nullable=false)
	private String dateString;
	
    @Column(name = "player", nullable = false)
    private String player;
    
    @Column(name="balance", nullable=false)
    private Integer balance;
    
    @Column(name = "wins", nullable = true)
    private Short wins;
    
    @Column(name = "losses", nullable = true)
    private Short losses;
    
    @JsonIgnore
    @CreationTimestamp
    private Date createDateTime;
 
    @JsonIgnore
    @UpdateTimestamp
    private Date updateDateTime;
    
    public static SimpleDateFormat createDateFormatter() {
    	SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    	return sdf;
    }

	public Bots(String name, String currentDateString) {
		this.player = name;
		this.dateString = currentDateString;
		
		this.losses = 0;
		this.wins = 0;
		this.balance = GambleUtil.MINIMUM_BET;
	}
}
