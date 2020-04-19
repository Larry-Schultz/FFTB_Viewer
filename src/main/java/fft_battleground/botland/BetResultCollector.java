package fft_battleground.botland;

import java.util.List;

import fft_battleground.botland.model.BetResults;
import fft_battleground.event.model.ResultEvent;
import lombok.Data;

@Data
public class BetResultCollector {

	private BotlandHelper helper;
	private BetterBetBot primaryBot;
	private List<BetterBetBot> subordinateBots;
	
	public BetResultCollector(BotlandHelper helper, BetterBetBot primaryBot, List<BetterBetBot> subordinateBots) {
		this.helper = helper;
		this.primaryBot = primaryBot;
		this.subordinateBots = subordinateBots;
	}
	
	public BetResults getResult(ResultEvent event) {
		BetResults results = null;
		results = new BetResults(helper.sortBetsBySide(), event.getWinner(), helper.getLeft(), helper.getRight(), helper.getBettingEndsEvent(), 
				helper.getMatchInfo(), helper.getTeamData());
		return results;
	}
}
