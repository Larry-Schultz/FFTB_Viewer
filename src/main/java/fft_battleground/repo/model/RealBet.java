package fft_battleground.repo.model;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import fft_battleground.bot.model.event.BettingEndsEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.BattleGroundTeamConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "real_bet_information")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class RealBet {

	@Id
	@SequenceGenerator(name="real_bet_information_generator", sequenceName = "real_bet_information_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "real_bet_information_generator")
    @Column(name = "real_bet_id", nullable = false)
	public Long id;
    
	@Convert(converter = BattleGroundTeamConverter.class)
	@Column(name = "left_team_name", nullable = false)
	public BattleGroundTeam leftTeamName;
	
	@Column(name = "left_team_bet_count", nullable = false)
	public Integer leftTeamBetCount;
	
	@Column(name = "left_team_amount", nullable = false)
	public Integer leftTeamAmount;
	
	@Convert(converter = BattleGroundTeamConverter.class)
	@Column(name = "right_team_name", nullable = false)
	public BattleGroundTeam rightTeamName;
	
	@Column(name = "right_team_bet_count", nullable = false)
	public Integer rightTeamBetCount;
	
	@Column(name = "right_team_amount", nullable = false)
	public Integer rightTeamAmount;
	
	@OneToOne(mappedBy = "realBetInformation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	public Match match;
	
	public RealBet() {}
	
    public RealBet(BettingEndsEvent fullBettingData, Match match) {
		this.leftTeamName = fullBettingData.getTeam1();
		this.leftTeamBetCount = fullBettingData.getTeam1Bets();
		this.leftTeamAmount = fullBettingData.getTeam1Amount();
		
		this.rightTeamName = fullBettingData.getTeam2();
		this.rightTeamBetCount = fullBettingData.getTeam2Bets();
		this.rightTeamAmount = fullBettingData.getTeam2Amount();
		
		this.match = match;
	}
}
