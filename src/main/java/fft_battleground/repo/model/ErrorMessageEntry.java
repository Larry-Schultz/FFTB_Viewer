package fft_battleground.repo.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "error_message")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class ErrorMessageEntry {

	@JsonIgnore
	@Id
	@SequenceGenerator(name="error_message_generator", sequenceName = "error_message_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_message_generator")
    @Column(name = "error_message_id", nullable = false)
	private Long errorMessageId;
	
	@Lob
	private String stackTrace;
	
    @CreationTimestamp
    private Date createDateTime;
    
    public ErrorMessageEntry() {
    	
    }
    
    public ErrorMessageEntry(Throwable e) {
    	this.stackTrace = ExceptionUtils.getStackTrace(e);
    }
}
