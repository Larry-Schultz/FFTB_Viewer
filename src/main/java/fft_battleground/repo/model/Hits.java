package fft_battleground.repo.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ColumnDefault;

import fft_battleground.repo.HitsType;

import lombok.Data;

@Entity
@Table(name = "hits")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
public class Hits {
	public static final String DISPLAY_FORMAT = "MM-dd-yyyy";
	
	@Id
	@SequenceGenerator(name="hits_generator", sequenceName = "hits_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hits_generator")
    @Column(name = "hits_day_id", nullable = false)
	private Long entryId;
	
	@Column(name="hits_type", nullable=false)
	@Enumerated(EnumType.STRING)
	private HitsType type;
	
	@Column(name="total", nullable=false)
	@ColumnDefault("0")
	private Integer total;
	
	@Column(name="day", nullable=false)
	private String day;
	
	public Hits() { }
	
	public Hits(HitsType type, String todaysString) {
		this.type = type;
		this.total = 0;
		this.day = todaysString;
	}
	
	public Hits(HitsType type, String todaysString, int initialCount) {
		this.type = type;
		this.total = initialCount;
		this.day = todaysString;
	}
}
