package fft_battleground.repo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fft_battleground.botland.model.BetResults;
import fft_battleground.botland.model.BetType;
import fft_battleground.event.detector.OtherPlayerBalanceDetector;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BettingEndsEvent;
import fft_battleground.event.model.OtherPlayerBalanceEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import fft_battleground.repo.model.Match;
import fft_battleground.repo.model.PlayerRecord;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@DataJpaTest
@SpringBootTest
@ComponentScan({ "fft_battleground" })
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto = create", "fft_battleground.interactive=false"})
@Slf4j
public class RepManagerTest {
	
    @Autowired
    private TestEntityManager entityManager;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private MatchRepo matchRepo;
	
	@Autowired
	private RepoManager repoManager;
	
	@Test
	public void testRepoWrite() {
		PlayerRecord player = null;
		log.info("Running Test");
		for(int i = 0; i < 2; i++) {
			Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById("OtherBrand");
			if(!maybeRecord.isPresent()) {
				player = new PlayerRecord("OtherBrand", 0, 1);
			} else {
				player = maybeRecord.get();
				player.setLosses(player.getLosses() + 1);
			}
			
			this.playerRecordRepo.save(player);
		}
		
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById("OtherBrand");
		assertTrue(maybeRecord.isPresent());
		assertTrue(maybeRecord.get().getLosses().equals(2));
		log.info("{}", maybeRecord.get().getLosses());
	}
	
	@Test
	public void testPlayerRepoWrite() {
		Pair<List<BetEvent>, List<BetEvent>> sampleBetEvents = new ImmutablePair<>(new ArrayList<>(), new ArrayList<>());
		BetEvent leftEvent = new BetEvent("OtherBrand", BattleGroundTeam.BLACK, "allin", "allin", BetType.ALLIN);
		BetEvent rightEvent = new BetEvent("datadrivenbot", BattleGroundTeam.GREEN, "100", "100", BetType.VALUE);
		BettingEndsEvent endsEvent = new BettingEndsEvent(BattleGroundTeam.BLACK, 3, 1000, BattleGroundTeam.GREEN, 4, 1750);
		BettingEndsEvent endsEvent2 = new BettingEndsEvent(BattleGroundTeam.BLACK, 2, 3000, BattleGroundTeam.GREEN, 5, 3750);
		BettingEndsEvent endsEvent3 = new BettingEndsEvent(BattleGroundTeam.BLACK, 1, 2000, BattleGroundTeam.GREEN, 6, 750);
		sampleBetEvents.getLeft().add(leftEvent);
		sampleBetEvents.getRight().add(rightEvent);
		BetResults testResult = new BetResults(sampleBetEvents, BattleGroundTeam.LEFT, BattleGroundTeam.BLACK, BattleGroundTeam.GREEN.BLACK, endsEvent, null, null);
		BetResults testResult2 = new BetResults(sampleBetEvents, BattleGroundTeam.RIGHT, BattleGroundTeam.BLACK, BattleGroundTeam.GREEN, endsEvent2, null, null);
		BetResults testResult3 = new BetResults(sampleBetEvents, BattleGroundTeam.LEFT, BattleGroundTeam.BLACK, BattleGroundTeam.GREEN, endsEvent3, null, null);
		
		this.repoManager.updatePlayerData(testResult);
		this.repoManager.updatePlayerData(testResult2);
		this.repoManager.updatePlayerData(testResult3);
		
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById("OtherBrand");
		assertTrue(maybeRecord.isPresent());
		assertTrue(maybeRecord.get().getWins().equals(2));
		assertTrue(maybeRecord.get().getLosses().equals(1));
	}
	
	@Test
	public void testMatchRepoWrite() {
		Pair<List<BetEvent>, List<BetEvent>> sampleBetEvents = new ImmutablePair<>(new ArrayList<>(), new ArrayList<>());
		BetEvent leftEvent = new BetEvent("OtherBrand", BattleGroundTeam.BLACK, "allin", "allin", BetType.ALLIN);
		BetEvent rightEvent = new BetEvent("datadrivenbot", BattleGroundTeam.GREEN, "100", "100", BetType.VALUE);
		sampleBetEvents.getLeft().add(leftEvent);
		sampleBetEvents.getRight().add(rightEvent);
		
		BettingEndsEvent endsEvent = new BettingEndsEvent(BattleGroundTeam.BLACK, 3, 1000, BattleGroundTeam.GREEN, 4, 1750);
		BetResults testResult = new BetResults(sampleBetEvents, BattleGroundTeam.LEFT, BattleGroundTeam.BLACK, BattleGroundTeam.GREEN, endsEvent, null, null);
		
		this.repoManager.getRepoTransactionManager().updateMatchData(testResult);
		List<Match> matches = this.matchRepo.findAll();
		assertNotNull(matches);
		assertTrue(matches.size() >= 1);
		assertNotNull(matches.get(0).getBets());
		assertTrue(matches.get(0).getBets().size() == 2);
		log.info("The left team amount = {}", matches.get(0).getRealBetInformation().getLeftTeamAmount());
		assertTrue(matches.get(0).getRealBetInformation().getLeftTeamAmount().equals(1000));
	}
	
	@Test
	public void testOtherPlayerBalanceDetector() {
		OtherPlayerBalanceDetector detector = new OtherPlayerBalanceDetector();
		ChatMessage message = new ChatMessage("FFTBattleground", "Bilabrin, your balance is: 292G (Spendable: 0G).; Logno, your balance is: 200G (Spendable: 100G).; Zeroroute, your balance is: 1,624G, with 194G currently tied up in a wager (Spendable: 982G).; helpimabug, your bet is 156G on brown. You currently hold a 0.9% share of your team's winnings, and stand to win 170G if you win.");
		BattleGroundEvent event = detector.detect(message);
		
		assertTrue(event instanceof OtherPlayerBalanceEvent);
		OtherPlayerBalanceEvent otherPlayerBalanceEvent = (OtherPlayerBalanceEvent) event;
		assertTrue(otherPlayerBalanceEvent != null);
		assertTrue(otherPlayerBalanceEvent.getOtherPlayerBalanceEvents() != null);
		assertTrue(otherPlayerBalanceEvent.getOtherPlayerBalanceEvents().size() == 3);
		assertTrue(otherPlayerBalanceEvent.getOtherPlayerBalanceEvents().get(1).getAmount().equals(200));
		log.info("{}", otherPlayerBalanceEvent.getOtherPlayerBalanceEvents().get(2).getPlayer());
		assertTrue(otherPlayerBalanceEvent.getOtherPlayerBalanceEvents().get(2).getPlayer().equals("Zeroroute"));
		assertTrue(otherPlayerBalanceEvent.getOtherPlayerBalanceEvents().get(0).getSpendable().equals(0));
		
		ChatMessage message2 = new ChatMessage("FFTBattleground", "Dakren, you advanced to Level 32! Your gil floor has increased to 328!");
		event = detector.detect(message2);
		assertNull(event);
	}
	
}
