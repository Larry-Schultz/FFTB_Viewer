package fft_battleground.dump.service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.controller.response.model.PlayerData;
import fft_battleground.dump.DumpReportsServiceImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.exception.CacheMissException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.image.model.Images;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.tournament.TournamentService;
import fft_battleground.util.GambleUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlayerPageDataServiceImpl implements PlayerPageDataService {
	
	@Autowired
	@Getter private TournamentService tournamentService;
	
	@Autowired
	private DumpService dumpService;

	@Autowired
	@Getter private DumpReportsServiceImpl dumpReportsService;
	
	@Autowired
	@Getter private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private GlobalGiServiceImpl globalGilUtil;
	
	@Autowired
	private Images images;

	@Override
	@Transactional(readOnly = true)
	public PlayerData getDataForPlayerPage(String playerName, TimeZone timezone) throws CacheMissException, TournamentApiException {
		PlayerData playerData = new PlayerData();
		String id = StringUtils.trim(StringUtils.lowerCase(playerName));
		Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(id);
		if(maybePlayer.isPresent()) {
			
			PlayerRecord record = maybePlayer.get();
			Hibernate.initialize(maybePlayer.get().getPlayerSkills());
			Hibernate.initialize(maybePlayer.get().getPrestigeSkills());
			for(PlayerSkills playerSkill : record.getPlayerSkills()) {
				playerSkill.setMetadata(StringUtils.replace(this.tournamentService.getCurrentTips().getUserSkill().get(playerSkill.getSkill()), "\"", ""));
			}
			for(PrestigeSkills prestigeSkill: record.getPrestigeSkills()) {
				prestigeSkill.setMetadata(StringUtils.replace(this.tournamentService.getCurrentTips().getUserSkill().get(prestigeSkill.getSkill()), "\"", ""));
			}
			playerData.setPlayerRecord(record);
			
			boolean isBot = this.dumpService.getBotCache().contains(record.getPlayer());
			playerData.setBot(isBot);
			
			if(StringUtils.isNotBlank(record.getPortrait())) {
				String portrait = record.getPortrait();
				String portraitUrl = this.images.getPortraitByName(portrait, record.getAllegiance());
				playerData.setPortraitUrl(portraitUrl);
			}
			if(StringUtils.isBlank(record.getPortrait()) || playerData.getPortraitUrl() == null) {
				if(playerData.isBot()) {
					playerData.setPortraitUrl(this.images.getPortraitByName("Steel Giant"));
				} else {
					playerData.setPortraitUrl(this.images.getPortraitByName("Ramza"));
				}
			}
			
			DecimalFormat df = new DecimalFormat("0.00");
			Double betRatio = ((double) 1 + record.getWins())/((double)1+ record.getWins() + record.getLosses());
			Double fightRatio = ((double)1 + record.getFightWins())/((double) record.getFightWins() + record.getFightLosses());
			String betRatioString = df.format(betRatio);
			String fightRatioString = df.format(fightRatio);
			playerData.setBetRatio(betRatioString);
			playerData.setFightRatio(fightRatioString);
			Integer betPercentile = this.dumpReportsService.getBetPercentile(betRatio);
			Integer fightPercentile = this.dumpReportsService.getFightPercentile(fightRatio);
			playerData.setBetPercentile(betPercentile);
			playerData.setFightPercentile(fightPercentile);
			
			
			boolean containsPrestige = false;
			int prestigeLevel = 0;
			Set<String> prestigeSkills = null;
			if(this.dumpService.getPrestigeSkillsCache().get(playerName) != null) {
				prestigeSkills = new HashSet<>(this.dumpService.getPrestigeSkillsCache().get(playerName));
				prestigeLevel = prestigeSkills.size();
				containsPrestige = true;
			}
			
			playerData.setContainsPrestige(containsPrestige);
			playerData.setPrestigeLevel(prestigeLevel);
			playerData.setPrestigeSkills(prestigeSkills);
			playerData.setExpRank(this.dumpService.getExpRankLeaderboardByPlayer().get(record.getPlayer()));
			
			DecimalFormat format = new DecimalFormat("##.#########");
			String percentageOfTotalGil = format.format(this.globalGilUtil.percentageOfGlobalGil(record.getLastKnownAmount()) * (double)100);
			playerData.setPercentageOfGlobalGil(percentageOfTotalGil);
			
			if(record.getLastActive() != null) {
				playerData.setTimezoneFormattedDateString(this.createDateStringWithTimezone(timezone, record.getLastActive()));
			}
			if(record.getLastFightActive() != null) {
				playerData.setTimezoneFormattedLastFightActiveDateString(this.createDateStringWithTimezone(timezone, record.getLastFightActive()));
			}
			
			Integer leaderboardRank = this.getLeaderboardPosition(playerName);
			playerData.setLeaderboardPosition(leaderboardRank);
			
			Set<String> classBonuses = this.dumpService.getClassBonusCache().get(playerName);
			playerData.setClassBonuses(classBonuses);
			
			Set<String> skillBonuses = this.dumpService.getSkillBonusCache().get(playerName);
			playerData.setSkillBonuses(skillBonuses);
		} else {
			playerData = new PlayerData();
			playerData.setNotFound(true);
			playerData.setPlayerRecord(new PlayerRecord());
			playerData.getPlayerRecord().setPlayer(GambleUtil.cleanString(playerName));
		}
			
		return playerData;
	}
	
	protected String createDateStringWithTimezone(TimeZone zone, Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");

		//Here you say to java the initial timezone. This is the secret
		sdf.setTimeZone(zone);
		//Will print in UTC
		String result = sdf.format(calendar.getTime());    

		return result;
	}
	
	protected Integer getLeaderboardPosition(String player) {
		String lowercasePlayer = StringUtils.lowerCase(player);
		Integer position = this.dumpService.getLeaderboard().get(lowercasePlayer);
		return position;
	}
}
