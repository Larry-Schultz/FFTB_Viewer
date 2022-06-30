package fft_battleground.event.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import fft_battleground.event.detector.model.AllegianceEvent;
import fft_battleground.event.detector.model.BadBetEvent;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.BetInfoEvent;
import fft_battleground.event.detector.model.BettingBeginsEvent;
import fft_battleground.event.detector.model.BettingEndsEvent;
import fft_battleground.event.detector.model.DontFightEvent;
import fft_battleground.event.detector.model.FightBeginsEvent;
import fft_battleground.event.detector.model.FightEntryEvent;
import fft_battleground.event.detector.model.InvalidFightEntryClassEvent;
import fft_battleground.event.detector.model.InvalidFightEntryCombinationEvent;
import fft_battleground.event.detector.model.InvalidFightEntrySexEvent;
import fft_battleground.event.detector.model.InvalidFightEntryTournamentStarted;
import fft_battleground.event.detector.model.MatchInfoEvent;
import fft_battleground.event.detector.model.MusicEvent;
import fft_battleground.event.detector.model.ResultEvent;
import fft_battleground.event.detector.model.SkillDropEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.event.detector.model.UnownedSkillEvent;
import fft_battleground.event.detector.model.fake.TournamentStatusUpdateEvent;
import lombok.Data;


@Data
@JsonTypeInfo(  
	    use = JsonTypeInfo.Id.NAME,  
	    include = JsonTypeInfo.As.EXISTING_PROPERTY,  
	    property = "eventType",
	    visible = true
)
@JsonSubTypes({        
    @Type(value = AllegianceEvent.class, name = "ALLEGIANCE"), 
    @Type(value = BadBetEvent.class, name = "BAD_BET"), 
    @Type(value = BetEvent.class, name = "BET"), 
    @Type(value = BetInfoEvent.class, name = "BET_INFO"), 
    @Type(value = BettingBeginsEvent.class, name = "BETTING_BEGINS"), 
    @Type(value = BettingEndsEvent.class, name = "BETTING_ENDS"), 
    @Type(value = DontFightEvent.class, name = "DONT_FIGHT"), 
    @Type(value = FightBeginsEvent.class, name = "FIGHT_BEGINS"), 
    @Type(value = FightEntryEvent.class, name = "FIGHT_ENTRY"), 
    @Type(value = InvalidFightEntryClassEvent.class, name = "INVALID_FIGHT_ENTRY_CLASS"), 
    @Type(value = InvalidFightEntryCombinationEvent.class, name = "INVALID_FIGHT_ENTRY_COMBINATION"), 
    @Type(value = InvalidFightEntrySexEvent.class, name = "INVALID_FIGHT_ENTRY_SEX"), 
    @Type(value = InvalidFightEntryTournamentStarted.class, name = "INVALID_FIGHT_ENTRY_TOURNAMENT_STARTED"), 
    @Type(value = MatchInfoEvent.class, name = "MATCH_INFO"), 
    @Type(value = MusicEvent.class, name = "MUSIC"), 
    @Type(value = ResultEvent.class, name = "RESULT"), 
    @Type(value = SkillDropEvent.class, name = "SKILL_DROP"), 
    @Type(value = TeamInfoEvent.class, name = "TEAM_INFO"), 
    @Type(value = UnitInfoEvent.class, name = "UNIT_INFO"), 
    @Type(value = UnownedSkillEvent.class, name = "UNOWNED_SKILL"), 
    @Type(value = TournamentStatusUpdateEvent.class, name = "TOURNAMENT_STATUS_UPDATE_EVENT"),
    })  
public abstract class BattleGroundEvent {
	protected BattleGroundEventType eventType;
	protected Date eventTime;

	public BattleGroundEvent() {}
	
	public BattleGroundEvent(BattleGroundEventType type) {
		this.eventType = type;
		this.eventTime = new Date();
	}
	
	public abstract String toString();
}
