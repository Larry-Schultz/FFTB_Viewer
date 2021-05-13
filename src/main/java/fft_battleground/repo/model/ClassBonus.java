package fft_battleground.repo.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Table(name = "class_bonus", indexes= {
		@Index(columnList = "player", name = "player_name_class_bonus_idx")})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
public class ClassBonus {
	
	private static final String[] humanClasses = new String[] {
			"Squire", "Knight", "Archer", "Monk", "Priest", "Wizard", "Chemist", "TimeMage", 
			"Summoner", "Thief", "Mediator", "Oracle", "Geomancer", "Lancer", "Samurai", "Ninja",
			"Dancer", "Bard", "Mime", "Calculator"};
	
	@JsonIgnore
	@Id
	@SequenceGenerator(name="class_bonus_generator", sequenceName = "class_bonus_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "class_bonus_generator")
    @Column(name = "class_bonus_id", nullable = false)
	private Long classBonusId;
	
	@Column(name = "player", nullable = false)
	private String player;
	
	@Column(name = "class_name", nullable = false)
	private String className;
	
    @CreationTimestamp
    @JsonIgnore
    private Date createDateTime;
    
    public ClassBonus() {}
    
    public ClassBonus(String player, String className) {
    	this.player = player;
    	this.className = className;
    }
    
    /**
     * To convert from the dump data to what the stream's bot outputs, we need to remove all
     * data from the data from a copy of the entire list of human classes.
     * 
     * This is because the dump only stores what classes have been used, but the bot outputs what classes
     * have not been used.
     * @param dumpOutput
     * @return
     */
    public static Set<String> convertToBotOutput(Collection<String> dumpOutput) {
    	Set<String> botOutput = getCopyOfHumanClasses();
    	botOutput.removeAll(dumpOutput);
    	
    	return botOutput;
    }
    
    /**
     * uses stream to create a deep copy
     * @return
     */
    protected static Set<String> getCopyOfHumanClasses() {
    	Set<String> copy = Arrays.<String>stream(humanClasses).collect(Collectors.toSet());
    	return copy;
    }
}
