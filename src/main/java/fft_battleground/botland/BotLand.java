package fft_battleground.botland;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;
import org.thymeleaf.util.StringUtils;

import fft_battleground.botland.bot.BetterBetBot;
import fft_battleground.botland.model.Bet;
import fft_battleground.botland.personality.PersonalityModuleFactory;
import fft_battleground.botland.personality.PersonalityResponse;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.detector.model.BadBetEvent;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.BetInfoEvent;
import fft_battleground.event.detector.model.BettingBeginsEvent;
import fft_battleground.event.detector.model.BettingEndsEvent;
import fft_battleground.event.detector.model.MatchInfoEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.model.Bots;
import fft_battleground.repo.repository.BotsRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.util.Router;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class BotLand extends TimerTask {
	private BotlandHelper helper;
	private String ircName;
	private boolean acceptingBets = true;
	private boolean enableBetting = false;
	private boolean enablePersonality = false;
	
	//references
	protected Router<ChatMessage> chatMessageRouterRef;
	protected PlayerRecordRepo playerRecordRepo;
	protected BotsRepo botsRepo;
	protected BattleGroundEventBackPropagation battleGroundEventBackPropagationRef;
	protected PersonalityModuleFactory personalityModuleFactoryRef;
	protected Timer botlandTimerRef;
	
	//state data
	private BetterBetBot primaryBot;
	private List<BetterBetBot> subordinateBots;
	
	public BotLand(Integer currentAmountToBetWith, BettingBeginsEvent beginEvent, List<BetEvent> otherPlayerBets, Set<UnitInfoEvent> unitInfoEvents) {
		List<BetEvent> bets = new Vector<BetEvent>(otherPlayerBets);
		this.helper = new BotlandHelper(currentAmountToBetWith, beginEvent.getTeam1(), beginEvent.getTeam2(), bets, unitInfoEvents);
	}

	@Override
	@SneakyThrows
	public void run() {
		Pair<List<BetEvent>, List<BetEvent>> betsBySide = this.helper.sortBetsBySide();
		this.helper.setUnitsBySide(this.helper.sortUnitsBySide());
		if (this.primaryBot != null) {
			this.primaryBot.setBetsBySide(betsBySide);
			this.primaryBot.setCurrentAmountToBetWith(helper.getCurrentAmountToBetWith());
			this.primaryBot.setOtherPlayerBets(this.helper.getOtherPlayerBets());
			this.primaryBot.setUnitsBySide(this.helper.getUnitsBySide());
			this.primaryBot.setTeamData(this.helper.getTeamData());
			// get results from main bot
			Bet bet = primaryBot.call();
			// send results to irc
			this.sendBet(bet);
			// get personality
			PersonalityResponse personalityMessage = this.primaryBot.generatePersonalityResponse();
			if (personalityMessage != null && personalityMessage.isDisplay()) {
				this.personalityModuleFactoryRef.addBotResponse(this.primaryBot.getName(),
						personalityMessage.getResponse());
				if (this.enablePersonality) {
					this.sendPersonalityMessage(personalityMessage.getResponse());
				}
			}
		}
		
		//call subordinate bots
		if (this.subordinateBots != null) {
			for (int i = 0; i < this.subordinateBots.size(); i++) {
				try {
					BetterBetBot currentSubordinateBot = this.subordinateBots.get(i);
					currentSubordinateBot.setOtherPlayerBets(this.helper.getOtherPlayerBets());
					currentSubordinateBot.setBetsBySide(betsBySide);
					currentSubordinateBot.setUnitsBySide(this.helper.getUnitsBySide());
					currentSubordinateBot.setTeamData(this.helper.getTeamData());;
					Bots botDataFromDatabase = this.botsRepo.getBotByDateStringAndName(currentSubordinateBot.getDateFormat(), currentSubordinateBot.getName());
					currentSubordinateBot.setCurrentAmountToBetWith(botDataFromDatabase.getBalance());
					Bet secondaryBet = currentSubordinateBot.call();
					String secondaryPersonalityMessage = currentSubordinateBot.generatePersonalityResponse().getResponse();
					log.info("Subordinate bot {} created bet with {} and replies \"{}\"", currentSubordinateBot.getName(), secondaryBet.generateBetString(), 
							secondaryPersonalityMessage != null ? secondaryPersonalityMessage : "none");
					if(secondaryPersonalityMessage != null) {
						this.personalityModuleFactoryRef.addBotResponse(currentSubordinateBot.getName(), secondaryPersonalityMessage);
					}
				} catch(Exception e) {
					log.error("something went wrong with one of the subordinate bots", e);
				}
			}
		} else {
			log.warn("no secondary bots found");
		}
		
		//send all data to repo manager
		
	}
	
	public BetResultCollector createCollector() {
		BetResultCollector collector = new BetResultCollector(this.helper, this.primaryBot, this.subordinateBots);
		return collector;
	}
	
	public void addBet(BetEvent bet) {
		if(this.acceptingBets) {
			synchronized(this.helper.getOtherPlayerBets()) {
				Optional<BetEvent> preExistingEvent = this.helper.getOtherPlayerBets().stream().filter(betEvent -> StringUtils.equalsIgnoreCase(betEvent.getPlayer(), bet.getPlayer())).findFirst();
				if(preExistingEvent.isPresent()) {
					 this.helper.getOtherPlayerBets().remove(preExistingEvent.get());
				}
				 this.helper.getOtherPlayerBets().add(bet);
			}
		}
	}
	
	public void addBetInfo(BetInfoEvent betInfo) {
		BetEvent bet = new BetEvent(betInfo);
		if(this.acceptingBets) {
			synchronized(this.helper.getOtherPlayerBets()) {
				Optional<BetEvent> preExistingEvent = this.helper.getOtherPlayerBets().stream().filter(betEvent -> StringUtils.equalsIgnoreCase(betEvent.getPlayer(), bet.getPlayer())).findFirst();
				if(preExistingEvent.isPresent()) {
					/*
					 * before we remove the pre-existing bet, let's take the chance to stash its subscriber status and send it to the betInfo object since even if we can't use the
					 * subscriber data (like if they bet random) we still get the subscriber data but we don't get that data from BetInfo, since the data source is the bot
					 * (and its always a subscriber)
					 */
					bet.setIsSubscriber(preExistingEvent.get().getIsSubscriber());
					this.helper.getOtherPlayerBets().remove(preExistingEvent.get());
				}
				
				
				this.helper.getOtherPlayerBets().add(bet);
			}
		}
	}
	
	public void removeBet(BadBetEvent event) {
		synchronized(this.helper.getOtherPlayerBets()) {
			for(String player : ((BadBetEvent) event).getPlayers()) {
				for(BetEvent bet : this.helper.getOtherPlayerBets()) {
					if(bet.getPlayer().equals(player)) {
						this.helper.getOtherPlayerBets().remove(bet);
						break;
					}
				}
			}
		}
	}
	
	public void addTeamInfo(TeamInfoEvent event) {
		if(this.getHelper().getLeft() == event.getTeam()) {
			this.helper.addTeamInfo(event);
		} else if(this.getHelper().getRight() == event.getTeam()) {
			this.helper.addTeamInfo(event);
		}
		this.helper.addTeamInfo(event);
	}
	
	public void addUnitInfo(UnitInfoEvent event) {
		this.helper.addUnitInfo(event);
	}
	
	public void setCurrentAmountToBetWith(Integer currentAmountToBetWith) {
		this.helper.setCurrentAmountToBetWith(currentAmountToBetWith);
	}
	
	public void setBettingEndsEvent(BettingEndsEvent event) {
		this.helper.setBettingEndsEvent(event);
	}
	
	public void setMatchInfo(MatchInfoEvent event) {
		this.helper.setMatchInfo(event);
	}
	
	@SneakyThrows
	protected void sendBet(Bet bet) {
		String betString = bet.generateBetString();
		log.info("Betting Enabled is {} for this server", this.enableBetting);
		if(this.enableBetting) {
			this.chatMessageRouterRef.sendDataToQueues(new ChatMessage(betString));
			this.battleGroundEventBackPropagationRef.SendUnitThroughTimer(new BetEvent(this.ircName, bet.getTeam(), String.valueOf(bet.getAmount()), String.valueOf(bet.getAmount()), bet.getType(), bet.getIsBettorSubscriber()));
		}
	}
	
	protected void sendPersonalityMessage(String personalityMessage) {
		if(personalityMessage != null) {
			this.botlandTimerRef.schedule(new PersonalityMessageTimerTask(personalityMessage, this.chatMessageRouterRef), 5000);
		}
	}
}

class PersonalityMessageTimerTask extends TimerTask {
	private String message;
	private Router<ChatMessage> chatMessageRouterRef;
	
	public PersonalityMessageTimerTask(String message, Router<ChatMessage> chatMessageRouterRef) {
		this.message = message;
		this.chatMessageRouterRef = chatMessageRouterRef;
	}
	
	@Override
	public void run() {
		this.chatMessageRouterRef.sendDataToQueues(new ChatMessage(this.message));
	}
}
