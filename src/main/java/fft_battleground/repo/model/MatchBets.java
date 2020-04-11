package fft_battleground.repo.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import fft_battleground.bot.model.event.BetEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.BattleGroundTeamConverter;
import fft_battleground.util.BooleanConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_bets")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@AllArgsConstructor
public class MatchBets {
	
	@Id
	@SequenceGenerator(name="match_bet_generator", sequenceName = "match_bet_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "match_bet_generator")
    @Column(name = "match_bet_id", nullable = false)
	private Long matchId;
	
	@Column(name = "player", nullable = false)
	private String player;
	
	@Convert(converter = BattleGroundTeamConverter.class)
	@Column(name = "teamName", nullable = false)
	private BattleGroundTeam teamName;
	
	@Column(name = "betAmount", nullable = false)
	private String betAmount;
	
	@Convert(converter = BooleanConverter.class)
	@Column(name = "win", nullable = false)
	private Boolean win;

	@ManyToOne
    @JoinColumn
	private Match match;
	
	public MatchBets() {}
	
	public MatchBets(BetEvent bet, boolean winningBet, Match match) {
		this.player = bet.getPlayer();
		this.teamName = bet.getTeam();
		this.betAmount = bet.getBetAmount();
		this.win = winningBet;
		
		this.match = match;
	}
}
