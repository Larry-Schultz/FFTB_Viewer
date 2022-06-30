package fft_battleground.botland.bot;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import fft_battleground.botland.bot.genetic.GeneFileCache;
import fft_battleground.botland.bot.genetic.model.BotGenome;
import fft_battleground.botland.bot.genetic.model.BraveFaithAttributes;
import fft_battleground.botland.bot.genetic.model.GeneTrainerV2BotData;
import fft_battleground.botland.bot.genetic.model.MapGeneAttributes;
import fft_battleground.botland.bot.genetic.model.PlayerDataGeneAttributes;
import fft_battleground.botland.bot.genetic.model.PotGeneAttributes;
import fft_battleground.botland.bot.genetic.model.SideGeneAttributes;
import fft_battleground.botland.bot.model.Bet;
import fft_battleground.botland.bot.util.BetterBetBot;
import fft_battleground.botland.bot.util.BotCanBetBelowMinimum;
import fft_battleground.botland.bot.util.BotCanInverse;
import fft_battleground.botland.bot.util.BotContainsPersonality;
import fft_battleground.botland.bot.util.BotParameterReader;
import fft_battleground.botland.bot.util.BotUsesGeneFile;
import fft_battleground.botland.model.BotParam;
import fft_battleground.discord.WebhookManager;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.MatchInfoEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.exception.BotConfigException;
import fft_battleground.exception.MissingGeneException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.tournament.classifier.UnitAttributeClassifier;
import fft_battleground.tournament.classifier.V2UnitAttributeClassifier;
import fft_battleground.tournament.model.Unit;
import fft_battleground.util.GambleUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneticBotV2 
extends BetterBetBot
implements BotCanBetBelowMinimum, BotContainsPersonality, BotCanInverse, BotUsesGeneFile {
	private static final String USE_BET_GENES_PARAM = "use_bet_genes";
	
	private String name;
	private BotGenome genes;
	private String filename;
	@Getter private boolean canBetBelowMinimum;
	@Getter private boolean useBetGenes;
	
	protected GeneFileCache<GeneTrainerV2BotData> geneFileCache;
	protected Collection<String> bots;
	protected WebhookManager noisyWebhookManager;
	
	protected UnitAttributeClassifier unitAttributeClassifier = new V2UnitAttributeClassifier();
	
	public GeneticBotV2(Integer currentAmountToBetWith, BattleGroundTeam left, BattleGroundTeam right,
			GeneFileCache<GeneTrainerV2BotData> geneFileCache, Collection<String> bots, WebhookManager noisyWebhookManager) {
		super(currentAmountToBetWith, left, right);

		this.geneFileCache = geneFileCache;
		this.bots = bots;
		this.noisyWebhookManager = noisyWebhookManager;
	}

	@Override
	public void initParams(Map<String, BotParam> map) throws BotConfigException {
		BotParameterReader reader = new BotParameterReader(map);
		this.filename = this.readGeneFileParameter(reader);
		this.personalityName = this.readPersonalityParam(reader);
		this.inverse = this.readInverseParameter(reader);
		this.canBetBelowMinimum = this.readCanBetBelowMinimumParameter(reader);
		this.useBetGenes = reader.readOptionalBooleanParam(USE_BET_GENES_PARAM);
		
		GeneTrainerV2BotData botData = this.geneFileCache.getGeneData(this.filename);
		this.genes = botData.getGenome();
		super.percentiles = botData.getPercentileMap();
	}

	@Override
	public void init() {
		
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	protected Float generateLeftScore() throws BotConfigException {
		Double score = 0d;
		score+= this.scoreFighters(this.teamData.getLeftTeamData());
		score+= this.scoreTeamAttributes(this.left);
		score+=this.scoreTeamBets(this.getBetsBySide().getLeft());
		score+=this.scorePot(this.getBetsBySide().getLeft(), this.getBetsBySide().getRight());
		return score.floatValue();
	}

	@Override
	protected Float generateRightScore() throws BotConfigException {
		Double score = 0d;
		score+= this.scoreFighters(this.teamData.getRightTeamData());
		score+= this.scoreTeamAttributes(this.right);
		score+=this.scoreTeamBets(this.getBetsBySide().getRight());
		score+=this.scorePot(this.getBetsBySide().getRight(), this.getBetsBySide().getLeft());
		return score.floatValue();
	}

	@Override
	protected Bet generateBetAmount(Float leftScore, Float rightScore, BattleGroundTeam chosenTeam) {
		float winnerScore = 0;
		float loserScore = 0;
		if(leftScore >= rightScore) {
			winnerScore = leftScore;
			loserScore = rightScore;
		} else if(rightScore > leftScore) {
			winnerScore = rightScore;
			loserScore = leftScore;
		}
		
		double scoreRatio = ((double) (winnerScore - loserScore + 1)) / ((double) winnerScore + 1);
		int ratioRounded = (int) Math.round(scoreRatio * 100);
		if(ratioRounded < 1) {
			//log.warn("ratio was less than 1! The ratio is {}", ratioRounded);
			ratioRounded = 1;
		}
		if(ratioRounded > 99) {
			//log.warn("ratio is greater than 99!  The ratio is {}", ratioRounded);
			ratioRounded = 99;
		}
		
		int floorBet = this.isBotSubscriber ? GambleUtil.SUBSCRIBER_MINIMUM_BET : GambleUtil.MINIMUM_BET;
		int betAmount = this.currentAmountToBetWith <= floorBet ? floorBet : this.currentAmountToBetWith;
		if(this.useBetGenes) {
			betAmount = this.genes.getBetGeneAttributes().getValue(ratioRounded);
		} else {
			betAmount = 1000;
		}
		betAmount = NumberUtils.min(this.currentAmountToBetWith, betAmount);
		
		Bet bet = new Bet(chosenTeam, betAmount, this.isBotSubscriber);
		
		return bet; 
	}
	
	protected Double scoreTeamAttributes(BattleGroundTeam actualTeam) throws BotConfigException {
		Double score = 0d;
		BattleGroundTeam side = actualTeam == this.left ? BattleGroundTeam.LEFT : BattleGroundTeam.RIGHT;
		SideGeneAttributes sideGeneAttributes = this.genes.getSideGeneAttributes();
		score+= sideGeneAttributes.getAttributeByTeam(actualTeam);
		score+= sideGeneAttributes.getAttributeByTeam(side);
		score+= this.scoreMap(matchInfo, side);
		
		List<Unit> units = side == BattleGroundTeam.LEFT ? this.unitsBySide.getLeft() : this.unitsBySide.getRight();
		score+= units.stream().map(unitAttributeClassifier::getUnitGeneAbilityElements)
				.flatMap(attributeList -> attributeList.stream())
				.filter(gene -> {
					boolean containsGene = this.genes.getMissingGeneAttributes().containsGene(gene);
					if(!containsGene) {
						log.warn("genefile is missing entry for attribute {}", gene);
					}
					return containsGene;
				})
				.mapToDouble(this.genes.getMissingGeneAttributes()::getGene)
				.sum();
		
		BraveFaithAttributes braveFaithAttributes = this.genes.getBraveFaithAttributes();
		for(Unit unit: units) {
			score+= unit.getFaith() * (braveFaithAttributes.faith() + braveFaithAttributes.braveFaith());
			score+= unit.getBrave() * (braveFaithAttributes.brave() + braveFaithAttributes.braveFaith());
			score+= unit.isRaidBoss() ? braveFaithAttributes.raidBoss() : 0d;
		}
		return score;
	}
	
	protected Double scoreMap(MatchInfoEvent matchInfo, BattleGroundTeam side) throws MissingGeneException {
		MapGeneAttributes mapGeneAttributes = this.genes.getMapGeneAttributes();
		double geneValue = 0;
		if(this.matchInfo != null && this.matchInfo.getMapNumber() != null) {
			int mapNumber = this.matchInfo.getMapNumber();
			Double gene = mapGeneAttributes.mapGene(side, mapNumber)
					.orElseThrow(this.createMissingMapGeneException(mapNumber, side));
			geneValue = gene.doubleValue();
		} else {
			this.noisyWebhookManager.sendMessage("Missing map data for bot: " + this.getName());
		}
		return geneValue;
	}
	
	protected Supplier<MissingGeneException> createMissingMapGeneException(int mapNumber, BattleGroundTeam side) {
		String message = MessageFormat.format("Cannot find gene for map {0} and side {1}", mapNumber, side);
		Supplier<MissingGeneException> supplier = () -> new MissingGeneException(message);
		return supplier;
	}
	
	protected Double scoreFighters(TeamInfoEvent teamInfoEvent) {
		Double score = 0d;
		PlayerDataGeneAttributes playerDataGeneAttributes = this.genes.getPlayerDataGeneAttributes();
		score += teamInfoEvent.getMetaData().stream()
				.filter(metadata -> metadata != null)
				.mapToDouble((metadata) -> {
					double wins = metadata.getFightWins() != null ? metadata.getFightWins().doubleValue() : 0;
					double losses = metadata.getFightLosses() != null ? metadata.getFightLosses().doubleValue() : 0;
					double fightRatio = (wins + 1d) / (wins + losses + 1d);
					return fightRatio * playerDataGeneAttributes.fightWinRatio();
		}).sum();
		
		int missingUnitMetadata = 4 - teamInfoEvent.getMetaData().size();
		if(missingUnitMetadata > 0) {
			score+= missingUnitMetadata * playerDataGeneAttributes.missingFightWinRatio() * playerDataGeneAttributes.fightWinRatio();
		}
		
		return score;
	}
	
	protected Double scoreTeamBets(List<BetEvent> bets) {
		Double score = 0d;
		//score bet ratios
		List<PlayerRecord> betEventMetadata = bets.stream().map(BetEvent::getMetadata).collect(Collectors.toList());
		PlayerDataGeneAttributes playerGenes = this.genes.getPlayerDataGeneAttributes();
		score += betEventMetadata.stream()
				.filter(metadata -> metadata != null)
				.mapToDouble((metadata) -> {
					double wins = metadata.getWins() != null ? metadata.getWins().doubleValue() : 0;
					double losses = metadata.getLosses() != null ? metadata.getLosses().doubleValue() : 0;
					double betRatio = (wins + 1d) / (wins + losses + 1d);
					return betRatio * playerGenes.betWinRatio();
		}).sum();
		
		//score subscribers
		score+= betEventMetadata.stream().filter(Objects::nonNull)
				.filter(PlayerRecord::isSubscriber).count() * playerGenes.subscriber();
		
		//score robots vs humans
		score+= betEventMetadata.stream().map(PlayerRecord::getPlayer)
				.mapToDouble(player -> this.bots.contains(player) ? playerGenes.robot() : playerGenes.human())
				.sum();
		
		return score;
	}
	
	protected Double scorePot(List<BetEvent> myTeamBet, List<BetEvent> otherTeamBet) {
		PotGeneAttributes potGenes = this.genes.getPotGeneAttributes();
		
		int myTeamSum = myTeamBet.stream().map(BetEvent::getBetAmount).mapToInt(Integer::valueOf).sum();
		int otherTeamSum = otherTeamBet.stream().mapToInt(BetEvent::getBetAmountInteger).sum();
		double odds = this.calculateOddsForTeam(myTeamSum, otherTeamSum);
		
		
		Double score = 0d;
		score+= myTeamSum * potGenes.potAmount();
		score+= myTeamBet.size() * potGenes.betCount();
		score+= odds * potGenes.odds();
		return score;
	}
	
	protected double calculateOddsForTeam(int yourTeamTotalValue, int theirTeamTotalValue) {
		if (yourTeamTotalValue == 0 || theirTeamTotalValue == 0) {
			return 0d;
		} else {
			double result =  ((double)theirTeamTotalValue)/((double)yourTeamTotalValue);
			return result;
		}
	}

}
