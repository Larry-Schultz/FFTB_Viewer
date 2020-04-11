package fft_battleground.repo.model;

import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.bot.model.event.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.Unit;
import fft_battleground.util.BattleGroundTeamConverter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "team_info", indexes= {
		@Index(columnList = "player", name = "player_name_idx")
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class TeamInfo {
	
    @Id
	@SequenceGenerator(name="team_info_generator", sequenceName = "team_info_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team_info_generator")
    @Column(name = "team_info_id", nullable = false)
	private Long id;
	
    @Column(name = "player", nullable = false)
	private String player;
    
    @Convert(converter = BattleGroundTeamConverter.class)
    @Column(name="team", nullable=true)
    private BattleGroundTeam team;
	
    @Column(name = "unit", nullable=false)
	@JsonProperty("Class")
	private String className;
    
    @Column(name="gender", nullable=true)
	@JsonProperty("Gender")
	private String Gender;
	
    @Column(name="sign", nullable=true)
	@JsonProperty("Sign")
	private String Sign;
	
    @Column(name="brave", nullable=true)
	@JsonProperty("Brave")
	private Short Brave;
	
    @Column(name="faith", nullable=true)
	@JsonProperty("Faith")
	private Short Faith;
	
    @Column(name="action_skill", nullable=true)
	@JsonProperty("ActionSkill")
	private String ActionSkill;
	
    @Column(name="reaction_skill", nullable=true)
	@JsonProperty("ReactionSkill")
	private String ReactionSkill;
	
    @Column(name="move_skill", nullable=true)
	@JsonProperty("MoveSkill")
	private String MoveSkill;
	
    @Column(name="mainhand", nullable=true)
	@JsonProperty("Mainhand")
	private String Mainhand;
	
    @Column(name="offhand", nullable=true)
	@JsonProperty("Offhand")
	private String Offhand;
	
    @Column(name="head", nullable=true)
	@JsonProperty("Head")
	private String Head;
	
    @Column(name="armor", nullable=true)
	@JsonProperty("Armor")
	private String Armor;
	
    @Column(name="accessory", nullable=true)
	@JsonProperty("Accessory")
	private String Accessory;
	
    @JoinColumn()
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonProperty("ClassSkills")
	private List<TeamInfoSkill> ClassSkills;
	
    @JoinColumn()
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonProperty("ExtraSkills")
	private List<TeamInfoSkill> ExtraSkills;
    
    @JoinColumn()
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
	private Match match;
    
    @JsonIgnore
    @CreationTimestamp
    private Date createDateTime;
    
    public TeamInfo() {}
    
    public TeamInfo(UnitInfoEvent unitInfo, BattleGroundTeam team, Match match) {
    	Unit unitData = unitInfo.getUnit();
    	this.player = StringUtils.lowerCase(unitData.getName());
    	this.className = unitData.getClassName();
    	this.Gender = unitData.getGender();
    	this.Sign = unitData.getSign();
    	this.Brave = unitData.getBrave();
    	this.Faith = unitData.getFaith();
    	this.ActionSkill = unitData.getActionSkill();
    	this.ReactionSkill = unitData.getReactionSkill();
    	this.MoveSkill = unitData.getMoveSkill();
    	this.Mainhand = unitData.getMainhand();
    	this.Offhand = unitData.getOffhand();
    	this.Head = unitData.getHead();
    	this.Armor = unitData.getArmor();
    	this.Accessory = unitData.getAccessory();
    	
    	Function<String, TeamInfoSkill> skillToTeamInfo = skill -> new TeamInfoSkill(skill, this);
    	this.ClassSkills = unitData.getClassSkills().stream().map(skillToTeamInfo).collect(Collectors.toList());
    	this.ExtraSkills = unitData.getExtraSkills().stream().map(skillToTeamInfo).collect(Collectors.toList());
    	
    	this.team = team;
    	this.match = match;
    }
}
