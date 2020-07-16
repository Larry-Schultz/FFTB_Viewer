package fft_battleground.repo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import fft_battleground.botland.model.BetType;
import fft_battleground.event.detector.BetDetector;
import fft_battleground.event.detector.BetInfoEventDetector;
import fft_battleground.event.detector.BettingEndsDetector;
import fft_battleground.event.detector.BuySkillDetector;
import fft_battleground.event.detector.FightDetector;
import fft_battleground.event.detector.GiftSkillDetector;
import fft_battleground.event.detector.LevelUpDetector;
import fft_battleground.event.detector.MusicDetector;
import fft_battleground.event.detector.OtherPlayerExpDetector;
import fft_battleground.event.detector.PlayerSkillDetector;
import fft_battleground.event.detector.PortraitEventDetector;
import fft_battleground.event.detector.PrestigeAscensionDetector;
import fft_battleground.event.detector.ResultEventDetector;
import fft_battleground.event.detector.RiserSkillWinDetector;
import fft_battleground.event.detector.SkillDropDetector;
import fft_battleground.event.detector.SkillWinEventDetector;
import fft_battleground.event.detector.TeamInfoDetector;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.event.model.BetInfoEvent;
import fft_battleground.event.model.BettingEndsEvent;
import fft_battleground.event.model.BuySkillEvent;
import fft_battleground.event.model.GiftSkillEvent;
import fft_battleground.event.model.LevelUpEvent;
import fft_battleground.event.model.MusicEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.event.model.PrestigeAscensionEvent;
import fft_battleground.event.model.ResultEvent;
import fft_battleground.event.model.RiserSkillWinEvent;
import fft_battleground.event.model.SkillDropEvent;
import fft_battleground.event.model.SkillWinEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import fft_battleground.util.GambleUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DetectorTests {

	@Test
	public void testLevelUpDetector() {
		LevelUpDetector detector = new LevelUpDetector();
		ChatMessage message = new ChatMessage("FFTBattleground",
				"Dakren, you advanced to Level 32! Your gil floor has increased to 328!");
		BattleGroundEvent event = detector.detect(message);
		assertNotNull(event);
		assertTrue(event instanceof LevelUpEvent);
		LevelUpEvent levelUpEvent = (LevelUpEvent) event;
		assertTrue(levelUpEvent.getPlayer().contentEquals("Dakren"));
		assertTrue(levelUpEvent.getLevel().equals((short) 32));

		ChatMessage message2 = new ChatMessage("FFTBattleground", "Bilabrin, your balance is: 292G (Spendable: 0G).");
		BattleGroundEvent event2 = detector.detect(message2);
		assertNull(event2);
	}

	@Test
	public void otherPlayerExpDetector() {
		String test1 = "Mesmaster, you are Level 23. You will Level Up when you gain another 82 EXP. You earn your next skill at Level 25, with 26 level skills remaining.; OtherBrand, The current track is: Fatal Fury Special - Geese Howard Theme. It will play for another 17 seconds.";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		OtherPlayerExpDetector detector = new OtherPlayerExpDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null);
		String test2 = "OtherBrand, you are Level 12. You will Level Up when you gain another 50 EXP. You earn your next skill at Level 13, with 30 level skills remaining.";
		message = new ChatMessage("fftbattleground", test2);
		event = detector.detect(message);
		assertTrue(event != null);
	}

	@Test
	public void testGambleUtil() {
		Integer team1 = 12656;
		Integer team2 = 45622;
		Float team1Odds = GambleUtil.bettingOdds(team1, team2);
		Float team2Odds = GambleUtil.bettingOdds(team2, team1);

		log.info("odds1: {}, odds2:{}", team1Odds, team2Odds);
	}

	@Test
	public void testBetDetector() {
		BetDetector detector = new BetDetector();
		ChatMessage message = new ChatMessage("OtherBrand", "!bet 57% red");
		ChatMessage message2 = new ChatMessage("allinbot", "!bet all blue");
		ChatMessage message3 = new ChatMessage("omnibotgamma", "!bet 100 random");
		ChatMessage message4 = new ChatMessage("minbetbot", "!allin yellow");
		ChatMessage message5 = new ChatMessage("magicbottle", "!betf red");
		ChatMessage message6 = new ChatMessage("thekillernacho", "!bet floor purple");
		ChatMessage message7 = new ChatMessage("lydian_c", "!allbut 10% champ");

		BetEvent event = (BetEvent) detector.detect(message);
		assertTrue(event != null);
		assertTrue(event.getBetType() == BetType.PERCENTAGE);

		event = (BetEvent) detector.detect(message2);
		assertTrue(event != null);
		assertTrue(event.getBetType() == BetType.ALLIN);

		event = (BetEvent) detector.detect(message3);
		assertTrue(event != null);
		assertTrue(event.getBetType() == BetType.VALUE);

		event = (BetEvent) detector.detect(message4);
		assertTrue(event != null);
		assertTrue(event.getBetType() == BetType.ALLIN);
		
		event = (BetEvent) detector.detect(message5);
		assertTrue(event != null);
		assertTrue(event.getBetType() == BetType.FLOOR);
		
		event = (BetEvent) detector.detect(message6);
		assertTrue(event != null);
		assertTrue(event.getBetType() == BetType.FLOOR);
		
		event = (BetEvent) detector.detect(message7);
		assertTrue(event != null);
		assertTrue(event.isAllinbutFlag());
		assertTrue(event.getBetType() == BetType.PERCENTAGE);
	}

	@Test
	public void testTeamInfoDetector() {
		String test1 = "Champion Team (Green): PrudishDuckling the Bard, RjA0zcOQ96 the Knight, Zebobz the Lancer, Aka Gilly the Malboro";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		TeamInfoDetector teamInfoDetector = new TeamInfoDetector();
		BattleGroundEvent event = teamInfoDetector.detect(message);
		assertTrue(event != null && event instanceof TeamInfoEvent && ((TeamInfoEvent) event).getTeam() != null);

		String test1a = "Champion Team (Blue): Lodrak the Archer, FreedomNM the Knight, L2 Sentinel the Lancer, BurningSaph the Wizard";
		message = new ChatMessage("fftbattleground", test1a);
		event = teamInfoDetector.detect(message);
		assertTrue(event != null && event instanceof TeamInfoEvent && ((TeamInfoEvent) event).getTeam() != null);

		String test2 = "Red Team: BlackFireUK the Wizard, Alacor the Knight, Numbersborne the Lancer, Shalloween the Squire";
		message = new ChatMessage("fftbattleground", test2);
		event = teamInfoDetector.detect(message);
		assertTrue(event != null && event instanceof TeamInfoEvent);
	}

	@Test
	public void testBuySkillDetector() {
		String test1 = " ShintaroNayaka, your bettable balance is: 14,691G (Spendable: 14,243G).; Angelomortis, unknown Team Name, should be: red, blue, green, yellow, white, black, purple, brown, or champion.; Baron_von_Scrub, you already own the EquipArmor skill!; OtherBrand, you bought the EquipArmor skill for 1,000G. Your new balance is 318G.";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		BuySkillDetector detector = new BuySkillDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null && event instanceof BuySkillEvent);
	}

	@Test
	public void testPlayerSkillDetector() {
		String test1 = "friendsquirrel, your skills: Wyvern, DualWield, Jump, Throw, Grenade, Cockatrice, HighlySkilled, Hydra, ArchaicDemon, DrawOut, Sicken, Reaper, IronHawk, ShortCharge, BlueDragon, EquipSword, Serpentarius, BlueMagic, MonsterTalk, AngelRing, EXPBoost, GreatMalboro, Gobbledeguck, PunchArt, DragonSpirit. (page 1 of 2)";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		PlayerSkillDetector playerSkillDetector = new PlayerSkillDetector();
		BattleGroundEvent event = playerSkillDetector.detect(message);
		assertTrue(event != null && event instanceof PlayerSkillEvent
				&& ((PlayerSkillEvent) event).getSkills().size() > 0);

		String test2 = "OtherBrand, your skills: Teleport, Parry, BlueDragon, Fly, HalveMP, EquipSword, RedDragon, BlackMagic, LongStatus, Dance, PunchArt";
		message = new ChatMessage("fftbattleground", test2);
		event = playerSkillDetector.detect(message);
		assertTrue(event != null && event instanceof PlayerSkillEvent
				&& ((PlayerSkillEvent) event).getSkills().size() > 0);
	}

	@Test
	public void testSkillDropDetector() {
		String test1 = "The current Skill Drop is: Caution (When you take damage, gain Defend, which increases your evasion.).";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		SkillDropDetector detector = new SkillDropDetector();
		BattleGroundEvent event = detector.detect(message);
		SkillDropEvent skillDropEvent = (SkillDropEvent) event;
		assertTrue(event != null && skillDropEvent.getSkill() != null && skillDropEvent.getSkillDescription() != null);
		
		String test2 = "The current Skill Drop is: FloatingEye (Allows you to use the monster: FloatingEye. Use !class to get info on the monster's stats. This is a normal monster (costs 200G). Rarity: Common.).";
		ChatMessage message2 = new ChatMessage("fftbattleground", test2); 
		event = detector.detect(message2);
		skillDropEvent = (SkillDropEvent) event;
		assertTrue(event != null && skillDropEvent.getSkill() != null);
	}

	@Test
	public void testPortraitDetector() {
		String test1 = "otherbrand, your Cheer Portrait was successfully set to chocobo.";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		PortraitEventDetector detector = new PortraitEventDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null && event instanceof PortraitEvent);
		PortraitEvent portraitEvent = (PortraitEvent) event;
		assertTrue(portraitEvent.getPlayer() != null && portraitEvent.getPortrait() != null
				&& portraitEvent.getPlayer().equals("otherbrand") && portraitEvent.getPortrait().equals("chocobo"));
	}

	@Test
	public void testResultEventDetector() {
		String test1 = "The green team was victorious! Next match starting soon...";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		ResultEventDetector detector = new ResultEventDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null && event instanceof ResultEvent);
		ResultEvent resultEvent = (ResultEvent) event;
		assertTrue(resultEvent.getWinner() != null && resultEvent.getWinner() == BattleGroundTeam.GREEN);
	}

	@Test
	public void testSkillWinDetector() {
		String test1 = "Congratulations, mirtaiatana! You have been bestowed the VanishMantle skill free of charge! Additionally, winterharte has also received it from the subscriber-only pool!";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		SkillWinEventDetector detector = new SkillWinEventDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null && event instanceof SkillWinEvent);
		SkillWinEvent skillWinEvent = (SkillWinEvent) event;
		assertTrue(skillWinEvent.getSkillEvents().size() == 2
				&& skillWinEvent.getSkillEvents().get(0).getPlayer().equals("mirtaiatana"));
	}

	@Test
	public void testBetInfoEventDetector() {
		String test1 = "TheChainNerd, your bet is 1,297G on black. You hold a 5.0% share of your team's winnings, and stand to win 617G if you win.";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		BetInfoEventDetector detector = new BetInfoEventDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null && event instanceof BetInfoEvent);
		BetInfoEvent betInfoEvent = (BetInfoEvent) event;
		assertTrue(betInfoEvent.getPlayer() != null
				&& betInfoEvent.getPlayer().equals(StringUtils.lowerCase("TheChainNerd")));
		assertTrue(betInfoEvent.getTeam() != null && betInfoEvent.getTeam() == BattleGroundTeam.BLACK);
		assertTrue(betInfoEvent.getBetAmount() != null && betInfoEvent.getBetAmount().equals(1297));
		assertTrue(betInfoEvent.getPercentage() != null && betInfoEvent.getPercentage().equals("5.0"));
		assertTrue(betInfoEvent.getPossibleEarnings() != null && betInfoEvent.getPossibleEarnings().equals(617));
	}
	
	@Test
	public void testBettingEndsEventDetector() {
		String test1 = "Betting is closed. Final Bets: white - 95 bets for 746,141G; black - 78 bets for 286,413G... Good luck!";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		BettingEndsDetector detector = new BettingEndsDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null && event instanceof BettingEndsEvent);
	}

	@Test
	public void testFightBeginsEventDetector() {
		String test1 = "You may now !fight to enter the tournament! This tournament's Skill Drop is: Dragon. One random user using !fight (or !dontfight) will receive this skill. Alternately, you can buy the skill for 1,000G.";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		FightDetector detector = new FightDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null);
	}

	@Test
	public void testGiftSkillEventDetector() {
		String test1 = "Due to a generous donation from BirbBrainsBot, thekillernacho has been bestowed the Tiamat skill free of charge!";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		GiftSkillDetector detector = new GiftSkillDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null);
		assertTrue(event instanceof GiftSkillEvent);
		GiftSkillEvent giftSkillEvent = (GiftSkillEvent) event;
		assertTrue(giftSkillEvent.getGivingPlayer().equals("birbbrainsbot"));
	}

	@Test
	public void testPrestigeAscensionEventDetector() {
		String test1 = "OtherBrand, you close your eyes and strip your flesh away, ascending to a new level of prestige. Your gil floor has been increased by 100G, and you learned the Hidden Skill: Teleport2!";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		PrestigeAscensionDetector detector = new PrestigeAscensionDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null);
		assertTrue(event instanceof PrestigeAscensionEvent);
		PrestigeAscensionEvent prestigeAscensionEvent = (PrestigeAscensionEvent) event;
		assertTrue(prestigeAscensionEvent.getPrestigeSkillsEvent() != null);
		assertTrue(prestigeAscensionEvent.getPrestigeSkillsEvent().getPlayer().equals("otherbrand"));
		assertTrue(prestigeAscensionEvent.getPrestigeSkillsEvent().getSkills().size() == 1);
		assertTrue(prestigeAscensionEvent.getPrestigeSkillsEvent().getSkills().get(0).equals("Teleport2"));
	}
	
	@Test
	public void testMusicDetector() {
		String test1 = "Mesmaster, you are Level 23. You will Level Up when you gain another 82 EXP. You earn your next skill at Level 25, with 26 level skills remaining.; OtherBrand, The current track is: Fatal Fury Special - Geese Howard Theme. It will play for another 17 seconds.";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		MusicDetector detector = new MusicDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null && event instanceof MusicEvent);
		MusicEvent musicEvent = (MusicEvent) event;
		assertTrue(musicEvent.getDurationInSeconds() == 17);
		assertTrue(StringUtils.equals(musicEvent.getSongName(), "Fatal Fury Special - Geese Howard Theme"));
	}
	
	@Test
	public void testRiserSkillWinEvent() {
		String test1 = "TheKillerNacho, you learned the skill: BladeGrasp!";
		ChatMessage message = new ChatMessage("fftbattleground", test1);
		RiserSkillWinDetector detector = new RiserSkillWinDetector();
		BattleGroundEvent event = detector.detect(message);
		assertTrue(event != null && event instanceof RiserSkillWinEvent);
		RiserSkillWinEvent riserSkillEvent = (RiserSkillWinEvent) event;
		assertTrue(riserSkillEvent.getSkillEvents().size() > 0);
		assertTrue(StringUtils.equals("thekillernacho", riserSkillEvent.getSkillEvents().get(0).getPlayer()));
		assertTrue(riserSkillEvent.getSkillEvents().get(0).getSkills().size() > 0);
		assertTrue(StringUtils.equals("BladeGrasp", riserSkillEvent.getSkillEvents().get(0).getSkills().get(0)));
	}

}
