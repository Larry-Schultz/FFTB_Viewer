package fft_battleground.repo.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "bot_hourly_data", indexes= {
		@Index(columnList = "player", name = "hourly_player_idx"),
		@Index(columnList ="player,hour_value", name = "hourly_player_hour_value_idx")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class BotHourlyData {
	
	private static final String DATE_FORMAT = "HH:00";
	
	public BotHourlyData() {}
	
	public BotHourlyData(String player, Integer balance) {
		this.player = player;
		this.balance = balance;
		
		this.hourValue = getHourValueForCurrentTime();
		this.hourString = getHourStringByInt(this.hourValue);
	}
	
	@JsonIgnore
    @Id
	@SequenceGenerator(name="bot_hour_key_generator", sequenceName = "bot_hour_key_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bot_hour_key_generator")
    @Column(name = "bot_entry_id", nullable = false)
	private Long bot_entry_id;
	
	@Column(name = "player", nullable = false)
	private String player;
	
	@Column(name="hour_string", nullable=false)
	private String hourString;
	
	@Column(name="hour_value", nullable=false)
	private Integer hourValue;
	
	@Column(name="balance", nullable=false)
	private Integer balance;

    @JsonIgnore
    @CreationTimestamp
    private Date createDateTime;
    
    @JsonIgnore
    @UpdateTimestamp
    private Date updateDateTime;
    
    public static String dateFormat() {
    	return DATE_FORMAT;
    }
    
    public static SimpleDateFormat getDateFormat() {
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    	return sdf;
    }
    
    public static int getHourValueForCurrentTime() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		return hour;
    }
    
    public static String getHourStringByInt(int hourInt) {
    	SimpleDateFormat sdf = getDateFormat();
    	String result = sdf.format(new Date());
    	
    	return result;
    }
}
