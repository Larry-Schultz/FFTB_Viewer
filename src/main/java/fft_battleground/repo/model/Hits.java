package fft_battleground.repo.model;

import java.text.SimpleDateFormat;
import java.util.Date;

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

import fft_battleground.repo.util.HitsType;
import lombok.Data;

@Entity
@Table(name = "hits")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
public class Hits {
	public static final String DISPLAY_FORMAT = "MM-dd-yyyy";
	
	@Id
	@Column(name="entryId", nullable=false)
	private String entryId;
	
	@Column(name="hits_type", nullable=false)
	@Enumerated(EnumType.STRING)
	private HitsType type;
	
	@Column(name="total", nullable=false)
	@ColumnDefault("0")
	private Integer total;
	
	@Column(name="day", nullable=false)
	private String day;
	
	@CreationTimestamp
	private Date createTimestamp;
	
	@UpdateTimestamp
	private Date updateTimestamp;
	
	public Hits() { }
	
	public Hits(HitsType type) {
		this.day = Hits.getTodaysDateString();
		this.type = type;
		this.entryId = Hits.generateIdString(type, this.day);
		this.total = 0;
	}
	
	public Hits(HitsType type, String todaysString) {
		this.entryId = Hits.generateIdString(type, todaysString);
		this.type = type;
		this.total = 0;
		this.day = todaysString;
	}
	
	public Hits(HitsType type, String todaysString, int initialCount) {
		this.entryId = Hits.generateIdString(type, todaysString);
		this.type = type;
		this.total = initialCount;
		this.day = todaysString;
	}
	
	public static String getTodaysDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat(Hits.DISPLAY_FORMAT);
		Date today = new Date();
		String result = sdf.format(today);
		
		return result;
	}
	
	public static String getIdStringForToday(HitsType type) {
		String todaysString = Hits.getTodaysDateString();
		String result = Hits.generateIdString(type, todaysString);
		
		return result;
	}
	
	public void incrementByOne() {
		this.incrementCount(1);
	}
	
	public void incrementCount(int count) {
		if(this.total != null) {
			this.total = this.total + count;
		} else {
			this.total = count;
		}
	}
	
	protected static String generateIdString(HitsType type, String todaysString) {
		String result = type.toString() + todaysString;
		return result;
	}
}
