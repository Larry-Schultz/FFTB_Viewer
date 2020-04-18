package fft_battleground.botland;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fft_battleground.botland.bot.DataBetBot;
import fft_battleground.botland.bot.MinBetBot;
import fft_battleground.botland.bot.OddsBot;
import fft_battleground.event.BattleGroundEventBackPropagation;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BettingBeginsEvent;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.util.Router;
import lombok.Data;

@Component
@Data
public class BetBotFactory {
	
	@Value("${irc.username}")
	private String ircName;
	
	private Class primaryBotClassName = MinBetBot.class;
	private List<Class> subordinateBotClassesName;
	
	@Autowired
	private Router<ChatMessage> messageSenderRouter;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BattleGroundEventBackPropagation battleGroundEventBackPropagation;
	
	public BetBot create(Class clazz, Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent) {
		BetBot betBot = null;
		if(clazz.equals(DataBetBot.class)) {
			betBot = new DataBetBot(currentAmountToBetWith, otherPlayerBets, beginEvent.getTeam1(), beginEvent.getTeam2());
		} else if(clazz.equals(OddsBot.class)) {
			betBot = new OddsBot(currentAmountToBetWith, otherPlayerBets, beginEvent.getTeam1(), beginEvent.getTeam2());
		}
		
		betBot.setChatMessageRouterRef(this.getMessageSenderRouter());
		betBot.setPlayerRecordRepo(this.playerRecordRepo);
		betBot.setBattleGroundEventBackPropagationRef(this.battleGroundEventBackPropagation);
		
		return betBot;
	}
	
	public BotLand createBotLand(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent) {
		BotLand land = new BotLand(currentAmountToBetWith, beginEvent, otherPlayerBets);
		
		land.setChatMessageRouterRef(this.getMessageSenderRouter());
		land.setPlayerRecordRepo(this.playerRecordRepo);
		land.setBattleGroundEventBackPropagationRef(this.battleGroundEventBackPropagation);
		land.setPrimaryBot(this.createPrimaryBot(currentAmountToBetWith, otherPlayerBets, beginEvent));
		land.setIrcName(this.ircName);
		return land;
	}
	
	public BetterBetBot createPrimaryBot(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BettingBeginsEvent beginEvent) {
		BetterBetBot betBot = null;
		if(primaryBotClassName.equals(MinBetBot.class)) {
			betBot = new MinBetBot(currentAmountToBetWith, beginEvent.getTeam1(), beginEvent.getTeam2());
		}
		betBot.setPlayerRecordRepoRef(this.playerRecordRepo);
		
		return betBot;
	}

}
