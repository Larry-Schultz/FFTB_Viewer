package fft_battleground.repo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.bot.model.SkillType;
import fft_battleground.event.model.AllegianceEvent;
import fft_battleground.event.model.BalanceEvent;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.event.model.LastActiveEvent;
import fft_battleground.event.model.LevelUpEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.BattleGroundTeamConverter;
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
    private Integer wins;
    
    @Column(name = "losses", nullable = false)
    private Integer losses;
    
    @Column(name = "fight_wins", nullable = false)
    private Integer fightWins;
    
    @Column(name = "fight_losses", nullable = false)
    private Integer fightLosses;
    
    @Column(name="last_known_amount", nullable = true)
    private Integer lastKnownAmount;
    
    @Column(name="highest_known_amount", nullable=true)
    private Integer highestKnownAmount;
    
    @Column(name="last_known_level", nullable = true)
    private Short lastKnownLevel;
    
    @Column(name="last_known_remaining_exp", nullable=true)
    private Short lastKnownRemainingExp;
    
    @Column(name="prestige", nullable=true)
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
    
    @JsonIgnore
    @CreationTimestamp
    private Date createDateTime;
 
    @JsonIgnore
    @UpdateTimestamp
    private Date updateDateTime;
    
    public PlayerRecord() {}
    
    public PlayerRecord(String name) {
    	this.player = name;
    	this.wins = 0;
    	this.losses = 0;
    	this.playerSkills = new ArrayList<>();
    }
    
    public PlayerRecord(String name, Integer wins, Integer losses) {
    	this.player = name;
    	this.wins = wins;
    	this.losses = losses;
    	
    	this.fightLosses = 0;
    	this.fightWins = 0;
    	
    	this.playerSkills = new ArrayList<>();
    }
    
	public PlayerRecord(BalanceEvent event) {
		this.player = event.getPlayer();
		this.lastKnownAmount = event.getAmount();
		this.highestKnownAmount = event.getAmount();
		
		this.setDefaults();
	}
	
	public PlayerRecord(LevelUpEvent event) {
		this.player = event.getPlayer();
		this.lastKnownLevel = event.getLevel();
		
		this.setDefaults();
	}
	
	public PlayerRecord(ExpEvent event) {
		this.player = event.getPlayer();
		this.lastKnownLevel = event.getLevel();
		this.lastKnownRemainingExp = event.getRemainingExp();
		
		this.setDefaults();
	}
	
	public PlayerRecord(AllegianceEvent event) {
		this.player = event.getPlayer();
		this.allegiance = event.getTeam();
		
		this.setDefaults();
	}
	
	public PlayerRecord(PortraitEvent event) {
		this.player = event.getPlayer();
		this.portrait = event.getPortrait();
		
		this.setDefaults();
	}
	
	public PlayerRecord(LastActiveEvent event) {
		this.player = event.getPlayer();
		this.lastActive = event.getLastActive();
		
		this.setDefaults();
	}
    
    public void addPlayerSkill(String skill, SkillType type) {
    	if(!this.playerSkills.contains(skill)) {
    		this.playerSkills.add(new PlayerSkills(skill, type, this));
    	}
    }
    
    protected void setDefaults() {
    	this.fightLosses = 0;
    	this.fightWins = 0;
    	this.losses = 0;
    	this.wins = 0;
    	
    	if(this.lastKnownAmount == null) {
    		this.lastKnownAmount = GambleUtil.MINIMUM_BET;
    	}
    }
    
}
