package fft_battleground.repo.model;

import java.util.Date;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.util.hibernate.BooleanConverter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "batch_data", indexes= {
		@Index(columnList = "batch_data_entry_type", name = "batch_data_entry_type_idx")})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class BatchDataEntry {

	@JsonIgnore
    @Id
	@SequenceGenerator(name="batch_data_key_generator", sequenceName = "batch_data_key_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "batch_data_key_generator")
    @Column(name = "id", nullable = false)
	private Long Id;
	
	@Temporal(TemporalType.DATE)
    @Column(name="update_started", nullable=false)
	private Date updateStarted;
	
	@Temporal(TemporalType.DATE)
    @Column(name="update_complete", nullable=false)
	private Date updateComplete;
	
	@Column(name = "number_of_players_analyzed", nullable = false)
	private Integer numberOfPlayersAnalyzed;
	
	@Column(name = "number_of_players_updated", nullable = false)
	private Integer numberOfPlayersUpdated;
	
	@Column(name="batch_data_entry_type", nullable=false)
    @Enumerated(EnumType.STRING)
	private BatchDataEntryType batchDataEntryType;
	
    @Column(name="successful_run", nullable=false)
    @Convert(converter = BooleanConverter.class)
    private Boolean successfulRun;
    
    @Column(name="exception_type", nullable=true)
    private String exceptionType;
    
    @Column(name="exception_line", nullable=true)
    private String exceptionLine;
	
	public BatchDataEntry() {}
	
	public BatchDataEntry(BatchDataEntryType batchDataEntryType, Integer playersAnalyzed, int playersUpdated, Date startDate, Date endDate) {
		this.batchDataEntryType = batchDataEntryType;
		this.numberOfPlayersAnalyzed = playersAnalyzed;
		this.numberOfPlayersUpdated = playersUpdated;
		this.updateStarted = startDate;
		this.updateComplete = endDate;
		
		this.successfulRun = true;
		this.exceptionType = null;
		this.exceptionLine = null;
	}

	public BatchDataEntry(BatchDataEntryType batchDataEntryType, Integer playersAnalyzed, int playersUpdated, Date startDate, Date endDate, String exceptionType, Integer lineNumber) {
		this.batchDataEntryType = batchDataEntryType;
		this.numberOfPlayersAnalyzed = playersAnalyzed;
		this.numberOfPlayersUpdated = playersUpdated;
		this.updateStarted = startDate;
		this.updateComplete = endDate;
		
		this.successfulRun = false;
		this.exceptionType = exceptionType;
		this.exceptionLine = lineNumber != null ? lineNumber.toString() : null;
	}
	
}
