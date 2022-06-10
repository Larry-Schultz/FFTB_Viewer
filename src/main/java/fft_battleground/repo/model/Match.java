package fft_battleground.repo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.botland.bot.model.BetResults;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.hibernate.BattleGroundTeamConverter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "matches")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class Match {

	@JsonIgnore
    @Id
	@SequenceGenerator(name="match_generator", sequenceName = "match_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "match_generator")
    @Column(name = "match_id", nullable = false)
	private Long match_id;
    
    @Convert(converter = BattleGroundTeamConverter.class)
    @Column(name = "winning_team", nullable=false, length=10)
	private BattleGroundTeam winningTeam;
    
    @Convert(converter = BattleGroundTeamConverter.class)
    @Column(name = "left_team", nullable=false)
	private BattleGroundTeam leftTeam;
    
    @Convert(converter = BattleGroundTeamConverter.class)
    @Column(name="right_team", nullable=false)
	private BattleGroundTeam rightTeam;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<MatchBets> bets;
    
    @JoinColumn()
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private RealBet realBetInformation;
    
    @JoinColumn()
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private MatchInfo matchInfo;
    
    @JoinColumn()
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeamInfo> leftTeamUnitInfo;
    
    @JoinColumn()
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeamInfo> rightTeamUnitInfo;
    
    @JsonIgnore
    @CreationTimestamp
    private Date createDateTime;
    
    public Match() {}
    
    public Match(BetResults results) {
    	this.winningTeam = results.getWinningTeam();
    	this.leftTeam = results.getLeftTeam();
    	this.rightTeam = results.getRightTeam();
    	
    	this.bets = new ArrayList<MatchBets>();
    	boolean leftWins = results.getWinningTeam() == BattleGroundTeam.LEFT;
    	boolean rightWins = results.getWinningTeam() == BattleGroundTeam.RIGHT;
    	for(BetEvent bet : results.getBets().getLeft()) {
    		MatchBets matchBet = new MatchBets(bet, leftWins, this);
    		this.bets.add(matchBet);
    	}
    	for(BetEvent bet : results.getBets().getRight()) {
    		MatchBets matchBet = new MatchBets(bet, rightWins, this);
    		this.bets.add(matchBet);
    	}
    	
		/*
		 * if(results.getFullBettingData() != null) { this.realBetInformation = new
		 * RealBet(results.getFullBettingData(), this); }
		 */
    	
    	Predicate<UnitInfoEvent> teamInfoFilterLambda = unitData -> unitData != null && unitData.getUnit() != null;
    	if(results.getTeamData().getLeftTeamData() != null) {
    		List<TeamInfo> leftTeamInfoList = results.getTeamData().getRightUnitInfoEvents().stream().filter(teamInfoFilterLambda).map(unitData -> new TeamInfo(unitData, this.leftTeam, this)).collect(Collectors.toList());
    		
    		this.leftTeamUnitInfo = leftTeamInfoList;
    	}
    	
    	if(results.getTeamData().getRightTeamData() != null) {
    		List<TeamInfo> rightTeamInfoList = results.getTeamData().getLeftUnitInfoEvents().stream().filter(teamInfoFilterLambda).map(unitData -> new TeamInfo(unitData, this.rightTeam, this)).collect(Collectors.toList());
    		
    		this.rightTeamUnitInfo = rightTeamInfoList;
    	}
    }
	
}
