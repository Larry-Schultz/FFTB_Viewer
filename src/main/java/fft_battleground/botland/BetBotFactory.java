package fft_battleground.botland;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.botland.bot.DataBetBot;
import fft_battleground.botland.bot.ArbitraryBot;
import fft_battleground.botland.bot.BetCountBot;
import fft_battleground.botland.bot.OddsBot;
import fft_battleground.botland.model.BotData;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BettingBeginsEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.model.Bots;
import fft_battleground.repo.repository.BotsRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.util.Router;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Data
@Slf4j
public class BetBotFactory {
	private static final String BOT_DATA_CACHE_KEY = "botDataCacheKey";
	
	@Value("${irc.username}")
	private String ircName;
	
	@Value("${botfactory.primaryBotName}")
	private String primaryBotName;
	
	@Value("${botfactory.botIsSubscriber}")
	private Boolean isBotSubscriber;
	
	@Value("${botfactory.botFile}")
	private String botFile;
	
	@Value("${enableBetting}")
	private boolean enableBetting;
	
	@Autowired
	private Router<ChatMessage> messageSenderRouter;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BotsRepo botsRepo;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	private Cache<String, Map<String, BotData>> botDataCache = Caffeine.newBuilder()
			  .expireAfterWrite(5, TimeUnit.MINUTES)
			  .maximumSize(1)
			  .build();

	
	public BotLand createBotLand(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, Set<UnitInfoEvent> currentUnits, BettingBeginsEvent beginEvent) {
		BotLand land = new BotLand(currentAmountToBetWith, beginEvent, otherPlayerBets, currentUnits);
		
		land.setChatMessageRouterRef(this.getMessageSenderRouter());
		land.setPlayerRecordRepo(this.playerRecordRepo);
		land.setBotsRepo(this.botsRepo);
		land.setBattleGroundEventBackPropagationRef(this.battleGroundEventBackPropagation);
		land.setIrcName(this.ircName);
		land.setEnableBetting(this.enableBetting);
		
		this.attachBots(land, currentAmountToBetWith, otherPlayerBets, beginEvent);
		
		return land;
	}
	
	public synchronized Map<String, BotData> getBotDataMap() {
		Map<String, BotData> botData = this.botDataCache.getIfPresent(BOT_DATA_CACHE_KEY);
		if(botData == null) {
			botData = this.getBotDataMapFromXmlFile();
			this.botDataCache.put(BOT_DATA_CACHE_KEY, botData);
		}
		
		return botData;
	}
	
	private void attachBots(BotLand botland, Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent) {
		List<BotData> botData = this.getBotConfig();
		String botDateString = this.botsRepo.currentDateString();
		List<BetterBetBot> secondaryBots = this.createSecondaryBots(otherPlayerBets, beginEvent, botData, botDateString);
		
		Optional<BotData> maybePrimaryBotData = botData.stream().filter(botDataEntry -> StringUtils.equalsIgnoreCase(botDataEntry.getName(), this.primaryBotName)).findFirst();
		BetterBetBot primaryBot = null;
		if(!maybePrimaryBotData.isPresent() ) {
			log.error("No primary bot configuration found!");
		} else {
			primaryBot = this.createBot(otherPlayerBets, beginEvent, maybePrimaryBotData.get(), botDateString);
			log.info("primary bot is minbetbot");
		}
		
		botland.setPrimaryBot(primaryBot);
		botland.setSubordinateBots(secondaryBots);
		
		return;
	}
	
	private Map<String, BotData> getBotDataMapFromXmlFile() {
		Map<String, BotData> botDataMap = this.getBotConfig().parallelStream().collect(Collectors.toMap(BotData::getName, Function.identity()));
		return botDataMap;
	}
	
	private List<BotData> getBotConfig() {
		SecondaryBotConfig botConfig = new SecondaryBotConfig(this.botFile);
		List<BotData> botData = botConfig.parseXmlFile();
		
		return botData;
	}
	
	
	private List<BetterBetBot> createSecondaryBots(List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent, List<BotData> botData, String botDateString) {
		List<BetterBetBot> secondaryBots = new ArrayList<>();
		for(BotData data: botData) {
			BetterBetBot newBot = this.createBot(otherPlayerBets, beginEvent, data, botDateString);
			secondaryBots.add(newBot);
		}
		return secondaryBots;
	}
	
	private BetterBetBot createBot(List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent, BotData botData, String botDateString) {
		BetterBetBot betBot = null;
		
		Bots botDataFromDatabase = this.botsRepo.getBotByDateStringAndName(botDateString, botData.getName());
		if(botDataFromDatabase == null) {
			botDataFromDatabase = this.botsRepo.addNewBotForToday(botData.getName(), this.isBotSubscriber);
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
			betBot.setBotSubscriber(this.isBotSubscriber);
		}
		
		return betBot;
		
	}

}
