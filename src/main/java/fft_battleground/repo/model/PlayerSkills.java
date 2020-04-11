package fft_battleground.repo.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.bot.model.SkillType;

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "player_skills")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class PlayerSkills {

	@JsonIgnore
	@Id
	@SequenceGenerator(name="player_skill_generator", sequenceName = "player_skill_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_skill_generator")
    @Column(name = "player_skill_id", nullable = false)
	private Long match_id;
    
    @Column(name="skill", nullable = false)
    private String skill;
    
    @Column(name="skill_type", nullable=false)
    @Enumerated(EnumType.STRING)
    private SkillType skillType;
    
    @JsonIgnore
	@ManyToOne
    @JoinColumn
    private PlayerRecord player_record;
    
    private String metadata;
	
    public PlayerSkills() {}
    
    public PlayerSkills( String skill, SkillType type, PlayerRecord player_record) {
		this.skill = skill;
		this.skillType = type;
		this.player_record = player_record;
	}
}
