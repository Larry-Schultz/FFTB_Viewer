package fft_battleground.repo;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fft_battleground.botland.SecondaryBotConfig;
import fft_battleground.botland.model.BotData;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.TournamentService;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.util.GambleUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class DumpTests {

	@Test
	public void testBotlandLoad() {
		SecondaryBotConfig config = new SecondaryBotConfig("Botland.xml");
		List<BotData> botData = config.parseXmlFile();
		assertTrue(config != null && botData != null);
		assertTrue(botData.size() > 0);
	}
	
	@Test
	public void testTournamentService() throws DumpException, TournamentApiException {
		TournamentService tournamentService = new TournamentService();
		Tournament currentTournament = tournamentService.getcurrentTournament();
		assertTrue(currentTournament != null);
	}
	
	@Test
	public void testExpressions() {
		Integer result = null;
		
		Argument leftScoreArg = new Argument("leftScore", 5f);
		Argument rightScoreArg = new Argument("rightScore", 10f);
		Argument minBet = new Argument("mnBet", GambleUtil.MINIMUM_BET);
		Argument maxBet = new Argument("mxBet", GambleUtil.MAX_BET);
		Argument balanceArg = new Argument("balance", 600);
		
		Expression exp = new Expression("min(mxBet, mnBet + 10 * (max(leftScore, rightScore) - min(leftScore, rightScore)), balance)", leftScoreArg, rightScoreArg, minBet, maxBet, balanceArg);
		if(!exp.checkSyntax() || !exp.checkLexSyntax()) {
			log.error("error with syntax: {}", exp.getErrorMessage());
			assertTrue(false);
		}
		
		result = new Double(exp.calculate()).intValue();
	}
	
}
