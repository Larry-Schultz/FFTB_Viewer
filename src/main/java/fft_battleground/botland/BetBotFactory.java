package fft_battleground.botland;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.BattleGroundEventBackPropagation;
import fft_battleground.botland.bot.DataBetBot;
import fft_battleground.botland.bot.MinBetBot;
import fft_battleground.botland.bot.OddsBot;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BettingBeginsEvent;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.Router;
import lombok.Data;

@Component
@Data
public class BetBotFactory {
	
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
		} else if(clazz.equals(MinBetBot.class)) {
			betBot = new MinBetBot(currentAmountToBetWith, otherPlayerBets, beginEvent.getTeam1(), beginEvent.getTeam2());
		} else if(clazz.equals(OddsBot.class)) {
			betBot = new OddsBot(currentAmountToBetWith, otherPlayerBets, beginEvent.getTeam1(), beginEvent.getTeam2());
		}
		
		betBot.setChatMessageRouterRef(this.getMessageSenderRouter());
		betBot.setPlayerRecordRepo(this.playerRecordRepo);
		betBot.setBattleGroundEventBackPropagationRef(this.battleGroundEventBackPropagation);
		
		return betBot;
	}

}
