package fft_battleground.repo.model;

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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Table(name = "skill_bonus", indexes= {
		@Index(columnList = "player", name = "player_name_skill_bonus_idx")})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
public class SkillBonus {
	
	@JsonIgnore
	@Id
	@SequenceGenerator(name="skill_bonus_generator", sequenceName = "skill_bonus_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skill_bonus_generator")
    @Column(name = "skill_bonus_id", nullable = false)
	private Long skillBonusId;
	
	@Column(name = "player", nullable = false)
	private String player;
	
	@Column(name = "skill", nullable = false)
	private String skill;
	
    @CreationTimestamp
    @JsonIgnore
    private Date createDateTime;
    
    public SkillBonus() {}
    
    public SkillBonus(String player, String skill) {
    	this.player = player;
    	this.skill = skill;
    }

}
