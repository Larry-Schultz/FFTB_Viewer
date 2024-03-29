package fft_battleground.tournament;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import fft_battleground.dump.data.AbstractDataProvider;
import fft_battleground.dump.data.DumpResourceManagerImpl;
import fft_battleground.event.detector.model.BetInfoEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentDumpServiceImpl
extends AbstractDataProvider
implements TournamentDumpService 
{
	private static final String prestigeSkillsMasterListUri = "http://www.fftbattleground.com/fftbg/PrestigeSkills.txt";
	private static final String raidBossUrlTemplateForTournament = "http://www.fftbattleground.com/fftbg/tournament_%s/raidboss.txt";
	private static final String tournamentPotUrlTemplate = "http://www.fftbattleground.com/fftbg/tournament_%1$s/pot%2$s.txt";
	private static final String entrantUrlTemplateForTournament = "http://www.fftbattleground.com/fftbg/tournament_%s/entrants.txt";
	private static final String winnersTxtUrlFormat = "http://www.fftbattleground.com/fftbg/tournament_%s/winner.txt";
	private static final String teamValueUrlFormat =  "http://www.fftbattleground.com/fftbg/tournament_%s/teamvalue.txt";
	private static final String streakUrlFormat =  "http://www.fftbattleground.com/fftbg/tournament_%s/streak.txt";
	private static final String memeUrlFormat = "http://www.fftbattleground.com/fftbg/tournament_%s/meme.txt";
	
	private static final String defaultMemeTourneySetting = "normal";
	
	public TournamentDumpServiceImpl(@Autowired DumpResourceManagerImpl dumpResourceManager) {
		super(dumpResourceManager);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<BattleGroundTeam> getWinnersFromTournament(Long id) throws DumpException {
		List<BattleGroundTeam> winners = new LinkedList<>();
		try(BufferedReader raidBossReader = this.getReaderForDumpTournamentUrlTemplate(winnersTxtUrlFormat, id)) {
			String line;
			while((line = raidBossReader.readLine()) != null) {
				if(StringUtils.isNotBlank(line)) {
					winners.add(BattleGroundTeam.parse(line));
				}
			}
		} catch (IOException e) {
			log.debug("reading winner data for tournament {} has failed", id);
			return new ArrayList<>();
		}
		
		return winners;
	}

	@Override
	public List<String> getRaidBossesFromTournament(Long id) throws DumpException {
		List<String> raidbosses = new LinkedList<>();
		try(BufferedReader raidBossReader = this.getReaderForDumpTournamentUrlTemplate(raidBossUrlTemplateForTournament, id)) {
			String line;
			while((line = raidBossReader.readLine()) != null) {
				if(StringUtils.isNotBlank(line)) {
					raidbosses.add(GambleUtil.cleanString(line));
				}
			}
		} catch (IOException e) {
			log.debug("reading raidboss data for tournament {} has failed", id);
			return new ArrayList<>();
		}
		
		return raidbosses;
	}

	@Override
	public List<BattleGroundEvent> parsePotFile(Long tournamentId, Integer potId) throws DumpException {
		List<BattleGroundEvent> result = new LinkedList<>();
		
		Resource resource;
		try {
			resource = new UrlResource(String.format(tournamentPotUrlTemplate, tournamentId.toString(), potId.toString()));
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
			return new ArrayList<>();
		}
		try(BufferedReader potFileReader = this.dumpResourceManager.openDumpResource(resource)) {
			potFileReader.readLine(); //read the summary line
			String line = null;
			String player = null;
			BattleGroundTeam currentTeam = null;
			while((line = potFileReader.readLine()) != null) {
				if(StringUtils.isNotBlank(line) && StringUtils.contains(line, " bets:")) { //if it contains bets:, it has the team name
					String teamString = StringUtils.substringBefore(line, " bets:");
					currentTeam = BattleGroundTeam.parse(teamString);
				} else if(StringUtils.isNotBlank(line) && !StringUtils.contains(line, " bets:")) { //otherwise it should be a player entry
					try {
						player = StringUtils.substringBefore(line, ":");
						player = GambleUtil.cleanString(player);
						String betAmountString = StringUtils.substringBetween(line, ": ", "G");
						betAmountString = StringUtils.replace(betAmountString, ",", "");
						Integer betAmount = Integer.valueOf(betAmountString);
						BetInfoEvent newBetInfoEvent = new BetInfoEvent(player, betAmount, currentTeam);
						result.add(newBetInfoEvent);
					} catch(NumberFormatException e) {
						//don't use data if the parsing fails
						log.warn("bet amount parsing failed for player {} in pot file {} for tournament {}", player, "pot" + potId.toString() + ".txt", tournamentId.toString());
					} 
				} 
			}
		} catch (IOException e1) {
			throw new DumpException(e1);
		}
		
		return result;
	}

	@Override
	public Set<String> parseEntrantFile(Long tournamentId) throws DumpException {
		Set<String> entrants = new HashSet<>();
		try(BufferedReader botReader = this.getReaderForDumpTournamentUrlTemplate(entrantUrlTemplateForTournament, tournamentId)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = line;
				cleanedString = StringUtils.replace(cleanedString, ",", "");
				cleanedString = StringUtils.trim(cleanedString);
				entrants.add(cleanedString);
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		return entrants;
	}

	@Override
	public Map<BattleGroundTeam, Integer> parseTeamValueFile(Long tournamentId) throws DumpException {
		Map<BattleGroundTeam, Integer> teamValues = new HashMap<>();
		try(BufferedReader botReader = this.getReaderForDumpTournamentUrlTemplate(teamValueUrlFormat, tournamentId)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = line;
				cleanedString = StringUtils.replace(cleanedString, ",", "");
				cleanedString = StringUtils.trim(cleanedString);
				String teamString = StringUtils.substringBefore(cleanedString, ":");
				BattleGroundTeam team = BattleGroundTeam.parse(teamString);
				String valueString = StringUtils.substringBetween(cleanedString, ": ", "G");
				Integer value = Integer.valueOf(valueString);
				teamValues.put(team, value);
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		List<BattleGroundTeam> coreTeams = new ArrayList<>(BattleGroundTeam.coreTeams());
		coreTeams.add(BattleGroundTeam.CHAMPION);
		for(BattleGroundTeam team: coreTeams) {
			if(!teamValues.containsKey(team)) {
				teamValues.put(team, 0);
			}
		}
		
		
		return teamValues;
	}

	@Override
	public Integer getChampionStreak(Long tournamentId) throws DumpException {
		Integer streak = null;
		try(BufferedReader botReader = this.getReaderForDumpTournamentUrlTemplate(streakUrlFormat, tournamentId)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = line;
				streak = Integer.valueOf(cleanedString);
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		
		return streak;
	}

	@Override
	public Set<String> getPrestigeSkillList() throws DumpException {
		Set<String> entrants = new HashSet<>();
		try(BufferedReader botReader = this.getReaderForUrl(prestigeSkillsMasterListUri)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = GambleUtil.cleanString(line);
				entrants.add(cleanedString);
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		return entrants;
	}
	
	@Override
	public List<String> getMemeTournamentSettings(Long id) throws DumpException {
		List<String> memeSettings = new LinkedList<>();
		try(BufferedReader memeReader = this.getReaderForDumpTournamentUrlTemplate(memeUrlFormat, id)) {
			String line;
			while((line = memeReader.readLine()) != null) {
				if(StringUtils.isNotBlank(line)) {
					memeSettings.add(GambleUtil.cleanString(line));
				}
			}
		} catch (IOException e) {
			log.debug("reading raidboss data for tournament {} has failed", id);
			return new ArrayList<>();
		}
		
		if(memeSettings.isEmpty()) {
			memeSettings.add(defaultMemeTourneySetting);
		}
		
		return memeSettings;
	}
	
	protected BufferedReader getReaderForDumpTournamentUrlTemplate(String template, Long id) throws DumpException, IOException {
		String url = this.processTournamentUrlTemplate(template, id);
		BufferedReader reader = this.getReaderForUrl(url);
		return reader;
	}
	
	protected String processTournamentUrlTemplate(String template, Long id) {
		String url = String.format(template, id.toString());
		return url;
	}

}
