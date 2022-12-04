package fft_battleground.tournament;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;

public interface TournamentDumpService {
	List<BattleGroundTeam> getWinnersFromTournament(final Long id) throws DumpException;
	List<String> getRaidBossesFromTournament(Long id) throws DumpException;
	List<BattleGroundEvent> parsePotFile(Long tournamentId, Integer potId) throws DumpException;
	Set<String> parseEntrantFile(Long tournamentId) throws DumpException;
	Map<BattleGroundTeam, Integer> parseTeamValueFile(Long tournamentId) throws DumpException;
	Integer getChampionStreak(Long tournamentId) throws DumpException;
	Set<String> getPrestigeSkillList() throws DumpException;
	List<String> getMemeTournamentSettings(Long tournamentId) throws DumpException;
}
