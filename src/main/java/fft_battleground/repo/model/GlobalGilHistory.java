package fft_battleground.repo.model;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.SneakyThrows;

@Entity
@Table(name = "global_gil_history")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
public class GlobalGilHistory implements Comparable<GlobalGilHistory> {

	public static final String dateFormatString = "dd-MM-yyyy";
	public static final String DISPLAY_FORMAT = "MM-dd-yyyy";
	
    @Id
    @Column(name = "date_string", nullable = false)
    @JsonIgnore
	private String date_string;
	
    @JsonIgnore
    @Column(name = "player_count", nullable = false)
	private Integer player_count;
	
    @JsonIgnore
    @Column(name = "global_gil_count", nullable = false)
	private Long global_gil_count;
    
    @JsonIgnore
    @CreationTimestamp
    private Date createDateTime;
    
    @JsonIgnore
    @UpdateTimestamp
    @Column(name="update_date_time")
    private Date updateDateTime;
    
    public GlobalGilHistory() {}
    
	public GlobalGilHistory(String currentDateString, Long globalGilCount, Integer globalPlayerCount) {
		this.date_string = currentDateString;
		this.player_count = globalPlayerCount;
		this.global_gil_count = globalGilCount;
	}
	
	@SneakyThrows
	@JsonProperty("dateWebFormat")
	public String displayDateWithWebFormat() {
		SimpleDateFormat pageFormat = new SimpleDateFormat(DISPLAY_FORMAT);
		Date historyDate = this.getDate();
		String newFormat = pageFormat.format(historyDate);
		return newFormat;
	}
	
	@JsonProperty("globalGilCountWebFormat")
	public String displayGlobalGilCountWithWebFormat() {
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setGroupingUsed(true);
		decimalFormat.setGroupingSize(3);
		String result = decimalFormat.format(this.global_gil_count);
		return result;
	}
	
	@JsonProperty("playerCountWebFormat")
	public String displayPlayerCountWithWebFormat() {
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setGroupingUsed(true);
		decimalFormat.setGroupingSize(3);
		String result = decimalFormat.format(this.player_count);
		return result;
	}
	
	@JsonProperty("gilPerPlayerWebFormat")
	public String displayGilPerPlayerWithWebFormat() {
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setGroupingUsed(true);
		decimalFormat.setGroupingSize(3);
		String result = decimalFormat.format(this.getGilPerPlayer());
		return result;
	}
	
	public Long getGilPerPlayer() {
		Long result = this.global_gil_count/this.player_count;
		return result;
	}
	
	@SneakyThrows
	public Date getDate() {
		String currentFormat = this.getDate_string();
		SimpleDateFormat globalGilFormat = new SimpleDateFormat(GlobalGilHistory.dateFormatString);
		Date historyDate = globalGilFormat.parse(currentFormat);
		return historyDate;
	}

	@Override
	public int compareTo(GlobalGilHistory arg0) {
		if(this.updateDateTime != null && arg0.getUpdateDateTime() != null) {
			return this.updateDateTime.compareTo(arg0.getUpdateDateTime());
		} else {
			return this.date_string.compareTo(arg0.getDate_string());
		}
	}
}
