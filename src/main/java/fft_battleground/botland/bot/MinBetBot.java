package fft_battleground.botland.bot;

import java.util.List;

import fft_battleground.botland.BetBot;
import fft_battleground.event.model.BetEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;

public class MinBetBot extends BetBot {

	public MinBetBot(Integer currentAmountToBetWith, List<BetEvent> otherPlayerBets, BattleGroundTeam left,
			BattleGroundTeam right) {
		super(currentAmountToBetWith, otherPlayerBets, left, right);
		// TODO Auto-generated constructor stub
	}

	private static final String NAME = "minBetBot";
	
	@Override
	public String getName() {
		return MinBetBot.NAME;
	}

	@Override
	protected Float generateLeftScore() {
		Float score = (float) this.betsBySide.getLeft().size();
		return score;
	}

	@Override
	protected Float generateRightScore() {
		Float score = (float) this.betsBySide.getRight().size();
		return score;
	}

	@Override
	protected Integer generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		return GambleUtil.MINIMUM_BET;
	}

}
