package fft_battleground.repo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.botland.model.SkillType;
import fft_battleground.event.model.AllegianceEvent;
import fft_battleground.event.model.BalanceEvent;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.event.model.FightEntryEvent;
import fft_battleground.event.model.LastActiveEvent;
import fft_battleground.event.model.LevelUpEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.event.model.SnubEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.util.SkillCategory;
import fft_battleground.repo.util.UpdateSource;
import fft_battleground.util.BattleGroundTeamConverter;
import fft_battleground.util.BooleanConverter;
import fft_battleground.util.GambleUtil;

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "player_record")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class PlayerRecord {
    
    @Id
    @Column(name = "player", nullable = false)
    private String player;
    
    @Column(name = "wins", nullable = false)
    @ColumnDefault("0")
    private Integer wins;
    
    @Column(name = "losses", nullable = false)
    @ColumnDefault("0")
    private Integer losses;
    
    @Column(name = "fight_wins", nullable = false)
    @ColumnDefault("0")
    private Integer fightWins;
    
    @Column(name = "fight_losses", nullable = false)
    @ColumnDefault("0")
    private Integer fightLosses;
    
    @Column(name="last_known_amount", nullable = true)
    @ColumnDefault("0")
    private Integer lastKnownAmount;
    
    @Column(name="highest_known_amount", nullable=true)
    @ColumnDefault("0")
    private Integer highestKnownAmount;
    
    @Column(name="last_known_level", nullable = true)
    @ColumnDefault("0")
    private Short lastKnownLevel;
    
    @Column(name="last_known_remaining_exp", nullable=true)
    @ColumnDefault("0")
    private Short lastKnownRemainingExp;
    
    @Column(name="prestige", nullable=true)
    @ColumnDefault("0")
    private Short lastKnownPrestige;
    
    @Convert(converter = BattleGroundTeamConverter.class)
    @Column(name="allegiance", nullable = true)
    private BattleGroundTeam allegiance;
    
    @Column(name="portrait", nullable=true, length=50)
    private String portrait;
    
    @OneToMany(mappedBy = "player_record", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PlayerSkills> playerSkills;
    
    @Temporal(TemporalType.DATE)
    @Column(name="last_active", nullable=true)
    private Date lastActive;
    
    @Temporal(TemporalType.DATE)
    @Column(name="last_fight_active", nullable=true)
    private Date lastFightActive;
    
    @Column(name="is_subscriber", nullable=true)
    @Convert(converter = BooleanConverter.class)
    private Boolean isSubscriber;
    
    @Column(name="current_snub_streak", nullable=true)
    @ColumnDefault("0")
    private Integer snubStreak;
    
    @CreationTimestamp
    @JsonIgnore
    private Date createDateTime;
 
    @UpdateTimestamp
    @JsonIgnore
    private Date updateDateTime;
    
    @Column(name="createdSource", nullable=true)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private UpdateSource createdSource;
    
    @Column(name="updateSource", nullable=true)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private UpdateSource updateSource;
    
    public PlayerRecord() {}
    
    public PlayerRecord(String name, UpdateSource createdSource) {
    	this.player = GambleUtil.cleanString(name);
    	this.wins = 0;
    	this.losses = 0;
    	this.playerSkills = new ArrayList<>();
    	this.createdSource = createdSource;
    }
    
    public PlayerRecord(String name, Integer wins, Integer losses, Boolean isSubscriber, UpdateSource createdSource) {
    	this.player = GambleUtil.cleanString(name);
    	this.wins = wins;
    	this.losses = losses;
    	
    	this.fightLosses = 0;
    	this.fightWins = 0;
    	
    	this.playerSkills = new ArrayList<>();
    	this.isSubscriber = isSubscriber;
    	this.createdSource = createdSource;
    }
    
	public PlayerRecord(BalanceEvent event, UpdateSource createdSource) {
		this.player = GambleUtil.cleanString(event.getPlayer());
		this.lastKnownAmount = event.getAmount();
		this.highestKnownAmount = event.getAmount();
		this.createdSource = createdSource;
		
		this.setDefaults();
	}
	
	public PlayerRecord(LevelUpEvent event, UpdateSource createdSource) {
		this.player = GambleUtil.cleanString(event.getPlayer());
		this.lastKnownLevel = event.getLevel();
		this.createdSource = createdSource;
		
		this.setDefaults();
	}
	
	public PlayerRecord(ExpEvent event, UpdateSource createdSource) {
		this.player = GambleUtil.cleanString(event.getPlayer());
		this.lastKnownLevel = event.getLevel();
		this.lastKnownRemainingExp = event.getRemainingExp();
		this.createdSource = createdSource;
		
		this.setDefaults();
	}
	
	public PlayerRecord(AllegianceEvent event, UpdateSource createdSource) {
		this.player = GambleUtil.cleanString(event.getPlayer());
		this.allegiance = event.getTeam();
		this.createdSource = createdSource;
		
		this.setDefaults();
	}
	
	public PlayerRecord(PortraitEvent event, UpdateSource createdSource) {
		this.player = GambleUtil.cleanString(event.getPlayer());
		this.portrait = event.getPortrait();
		this.createdSource = createdSource;
		
		this.setDefaults();
	}
	
	public PlayerRecord(LastActiveEvent event, UpdateSource createdSource) {
		this.player = GambleUtil.cleanString(event.getPlayer());
		this.lastActive = event.getLastActive();
		this.createdSource = createdSource;
		
		this.setDefaults();
	}
	
	public PlayerRecord(FightEntryEvent event, UpdateSource createdSource) {
		this.player = GambleUtil.cleanString(event.getPlayer());
		this.lastFightActive = event.getEventTime();
		this.updateSource = createdSource;
		
		this.setDefaults();
	}
	
	public PlayerRecord(SnubEvent event, UpdateSource updateSource) {
		this.player = GambleUtil.cleanString(event.getPlayer());
		this.snubStreak = event.getSnub();
		this.updateSource = updateSource;
		
		this.setDefaults();
	}
    
    public void addPlayerSkill(String skill, SkillType type) {
    	this.playerSkills.add(new PlayerSkills(skill, type, this));
    }
    
	public void addPlayerSkill(PlayerSkills playerSkills) {
		this.playerSkills.add(playerSkills);
		
	}
    
	public void addPlayerSkill(String skill, Integer cooldown, SkillType type) {
		this.playerSkills.add(new PlayerSkills(skill, cooldown, type, this));
	}
    
    protected void setDefaults() {
    	this.fightLosses = 0;
    	this.fightWins = 0;
    	this.losses = 0;
    	this.wins = 0;
    	this.snubStreak = 0;
    	this.isSubscriber = false;
    	
    	if(this.lastKnownAmount == null) {
    		this.lastKnownAmount = GambleUtil.getMinimumBetForBettor(this.isSubscriber());
    	}
    }
    
    public boolean isSubscriber() {
    	if(this.isSubscriber == null || !this.isSubscriber) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    public Integer prestigeLevel() {
    	Integer prestigeLevel = 0;
    	if(this.playerSkills != null) {
    		prestigeLevel = (int) this.playerSkills.parallelStream().filter(skill -> skill.getSkillType() == SkillType.PRESTIGE).count();
    	}
    	return prestigeLevel;
    }

}
