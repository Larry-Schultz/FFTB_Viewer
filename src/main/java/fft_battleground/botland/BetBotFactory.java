package fft_battleground.botland;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fft_battleground.botland.bot.DataBetBot;
import fft_battleground.botland.bot.ArbitraryBot;
import fft_battleground.botland.bot.BetCountBot;
import fft_battleground.botland.bot.OddsBot;
import fft_battleground.botland.model.BotData;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BettingBeginsEvent;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.BotsRepo;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.Bots;
import fft_battleground.util.Router;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Data
@Slf4j
public class BetBotFactory {
	
	@Value("${irc.username}")
	private String ircName;
	
	private Class primaryBotClassName = BetCountBot.class;
	
	@Autowired
	private Router<ChatMessage> messageSenderRouter;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BotsRepo botsRepo;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;

	
	public BotLand createBotLand(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent) {
		BotLand land = new BotLand(currentAmountToBetWith, beginEvent, otherPlayerBets);
		
		land.setChatMessageRouterRef(this.getMessageSenderRouter());
		land.setPlayerRecordRepo(this.playerRecordRepo);
		land.setBotsRepo(this.botsRepo);
		land.setBattleGroundEventBackPropagationRef(this.battleGroundEventBackPropagation);
		land.setPrimaryBot(this.createPrimaryBot(currentAmountToBetWith, otherPlayerBets, beginEvent));
		land.setIrcName(this.ircName);
		
		land.setSubordinateBots(new ArrayList<>());
		
		try {
			List<BetterBetBot> subordinateBots = this.createSecondaryBots(otherPlayerBets, beginEvent);
			land.setSubordinateBots(subordinateBots);
		} catch (Exception e) {
			log.error("Exception creating secondary bots", e);
			land.setSubordinateBots(new ArrayList<>());
		}
		
		return land;
	}
	
	public BetterBetBot createPrimaryBot(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent) {
		BetterBetBot betBot = null;
		if(primaryBotClassName.equals(BetCountBot.class)) {
			betBot = new BetCountBot(currentAmountToBetWith, beginEvent.getTeam1(), beginEvent.getTeam2());
		}
		betBot.setPlayerRecordRepoRef(this.playerRecordRepo);
		
		return betBot;
	}
	
	
	public List<BetterBetBot> createSecondaryBots(List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent) {
		List<BetterBetBot> secondaryBots = new ArrayList<>();
		String botDateString = this.botsRepo.currentDateString();
		
		SecondaryBotConfig botConfig = new SecondaryBotConfig("Botland.xml");
		List<BotData> botData = botConfig.parseXmlFile();
		for(BotData data: botData) {
			BetterBetBot newBot = this.createSecondaryBot(otherPlayerBets, beginEvent, data, botDateString);
			secondaryBots.add(newBot);
		}
		return secondaryBots;
	}
	
	public BetterBetBot createSecondaryBot(List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent, BotData botData, String botDateString) {
		BetterBetBot betBot = null;
		
		Bots botDataFromDatabase = this.botsRepo.getBotByDateStringAndName(botDateString, botData.getName());
		if(botDataFromDatabase == null) {
			botDataFromDatabase = this.botsRepo.addNewBotForToday(botData.getName());
			log.info("creating new bot data entry for: {} with dateString {}", botDataFromDatabase.getPlayer(), botDataFromDatabase.getDateString());
		}
		
		Integer currentAmountToBetWith = botDataFromDatabase.getBalance();
		
		if(StringUtils.equalsIgnoreCase(botData.getClassname(), "betcountbot")) {
			betBot = new BetCountBot(currentAmountToBetWith, beginEvent.getTeam1(), beginEvent.getTeam2());
		} else if(StringUtils.equalsIgnoreCase(botData.getClassname(), "databetbot")) {
			betBot = new DataBetBot(currentAmountToBetWith, beginEvent.getTeam1(), beginEvent.getTeam2());
		} else if(StringUtils.equalsIgnoreCase(botData.getClassname(), "oddsbot")) {
			betBot = new OddsBot(currentAmountToBetWith, beginEvent.getTeam1(), beginEvent.getTeam2());
		} else if(StringUtils.equalsIgnoreCase(botData.getClassname(), "arbitrarybot")) {
			betBot = new ArbitraryBot(currentAmountToBetWith, beginEvent.getTeam1(), beginEvent.getTeam2());
		} else {
			log.error("botData with data: {} failed", botData);
		}
		
		if(betBot != null) {
			betBot.initParams(botData.getParams());
			betBot.setName(botData.getName());
			betBot.setPlayerRecordRepoRef(this.playerRecordRepo);
			betBot.setDateFormat(botDateString);
		}
		
		return betBot;
		
	}

}
