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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.skill.model.Skill;
import fft_battleground.skill.model.SkillCategory;
import fft_battleground.skill.model.SkillType;
import fft_battleground.util.hibernate.BooleanConverter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "prestige_skills", indexes= {
		@Index(columnList = "player_record_player", name = "player_name_prestige_skill_table_idx")})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class PrestigeSkills implements Skill {

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
    
    @Column(name="skill_category", nullable=true)
    @Enumerated(EnumType.STRING)
    private SkillCategory skillCategory;
    
    @Column(name="cooldown", nullable=true)
    @ColumnDefault("0")
    private Integer cooldown;
    
    @Column(name="is_active", nullable=true)
    @Convert(converter = BooleanConverter.class)
    private Boolean isActive;
    
    @CreationTimestamp
    @JsonIgnore
    private Date createDateTime;
 
    @UpdateTimestamp
    @JsonIgnore
    private Date updateDateTime;
     
    @JsonIgnore
	@ManyToOne
    @JoinColumn
    private PlayerRecord player_record;
    
    private String metadata;
	
    public PrestigeSkills() {}
    
    public PrestigeSkills(Skill playerSkill) {
    	this.skill = playerSkill.getSkill();
    	this.skillType = playerSkill.getSkillType();
    	this.cooldown = playerSkill.getCooldown();
    	this.player_record = playerSkill.getPlayer_record();
    	this.skillCategory = SkillCategory.PRESTIGE;
    }
    
    public PrestigeSkills( String skill, SkillType type, PlayerRecord player_record) {
		this.skill = skill;
		this.skillType = type;
		this.cooldown = null;
		this.player_record = player_record;
		this.skillCategory = SkillCategory.NORMAL;
	}
    
    public PrestigeSkills(String skill, Integer cooldown, SkillType skillType) {
    	this.skill = skill;
    	this.skillType = skillType;
    	this.cooldown = cooldown;
    	this.skillCategory = SkillCategory.NORMAL;
    }
    
	public PrestigeSkills(String skill, int cooldown) {
		this.skill = skill;
		this.cooldown = cooldown;
		this.skillCategory = SkillCategory.NORMAL;
	}

	public PrestigeSkills(String skill) {
		this.skill = skill;
		this.cooldown = null;
		this.skillCategory = SkillCategory.NORMAL;
	}

	public PrestigeSkills(String skill, Integer cooldown, SkillType type, PlayerRecord playerRecord) {
		this.skill = skill;
		this.cooldown = cooldown;
		this.skillType = type;
		this.player_record = playerRecord;
	}
	
	public PrestigeSkills(String skill, Integer cooldown, SkillType type, SkillCategory skillCategory, PlayerRecord playerRecord) {
		this.skill = skill;
		this.cooldown = cooldown;
		this.skillType = type;
		this.skillCategory = skillCategory;
		this.player_record = playerRecord;
	}

}
