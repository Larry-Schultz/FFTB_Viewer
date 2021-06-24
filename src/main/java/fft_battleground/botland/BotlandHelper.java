package fft_battleground.botland;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.botland.model.Bet;
import fft_battleground.botland.model.TeamData;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.BettingEndsEvent;
import fft_battleground.event.detector.model.MatchInfoEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.tournament.model.Unit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class BotlandHelper {

	protected Integer currentAmountToBetWith;
	protected List<BetEvent> otherPlayerBets;
	protected Set<UnitInfoEvent> unitInfoEvents;
	protected Map<String, PlayerRecord> playerBetRecords;
	protected BattleGroundTeam left;
	protected BattleGroundTeam right;

	protected MatchInfoEvent matchInfo;
	protected TeamData teamData;
	protected BettingEndsEvent bettingEndsEvent;
	
	protected Pair<List<BetEvent>, List<BetEvent>> betsBySide;
	protected Pair<List<Unit>, List<Unit>> unitsBySide;
	protected Bet result;
	
	public BotlandHelper(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right, List<BetEvent> bets, Set<UnitInfoEvent> unitInfoEvents) {
		this.otherPlayerBets = bets;
		this.unitInfoEvents = unitInfoEvents;
		this.currentAmountToBetWith = currentAmountToBetWith;
		this.left = left;
		this.right = right;
		
		assert(this.currentAmountToBetWith.intValue() != 0);
		this.teamData = new TeamData();
	}
	
	//sort bets by left and right team
	public Pair<List<BetEvent>, List<BetEvent>> sortBetsBySide() {
		List<BetEvent> betEventCopy = null;
		synchronized(this.getOtherPlayerBets()) {
			betEventCopy = new Vector<>(this.otherPlayerBets);
		}
		Pair<List<BetEvent>, List<BetEvent>> eventsBySide = new ImmutablePair<>(new Vector<BetEvent>(), new Vector<BetEvent>());
		for(BetEvent betEvent: betEventCopy) {
			if(betEvent.getTeam() == this.left || betEvent.getTeam() == BattleGroundTeam.LEFT) {
				eventsBySide.getLeft().add(betEvent);
			} else if(betEvent.getTeam() == this.right || betEvent.getTeam() == BattleGroundTeam.RIGHT) {
				eventsBySide.getRight().add(betEvent);
			}
		}
		
		this.betsBySide = eventsBySide;
		
		return eventsBySide;
	}
	
	public Pair<List<Unit>, List<Unit>> sortUnitsBySide() {
		List<Unit> leftUnits = new ArrayList<>();
		List<Unit> rightUnits = new ArrayList<>();
		for(UnitInfoEvent event: this.unitInfoEvents) {
			if(event.getTeam() == this.left) {
				leftUnits.add(event.getUnit());
			} else if (event.getTeam() == this.right) {
				rightUnits.add(event.getUnit());
			}
		}
		
		Pair<List<Unit>, List<Unit>> result = new ImmutablePair<>(leftUnits, rightUnits);
		return result;
	}
	
	/**
	 * Adds team event without caller having to know which team is which
	 * 
	 * @param event
	 */
	public void addTeamInfo(TeamInfoEvent event) {
		if(event.getTeam() == this.left) {
			this.getTeamData().setLeftTeamData(event);
		} else if(event.getTeam() == this.right) {
			this.getTeamData().setRightTeamData(event);
		}
		
		return;
	}
	
	public void addUnitInfo(UnitInfoEvent event) {
		this.getTeamData().addUnitInfo(event);
		
		
		if(event.getTeam() == this.left) {
			
		}
	}

}
