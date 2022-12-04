package fft_battleground.dump.service;

import java.util.TimeZone;

import org.springframework.transaction.annotation.Transactional;

import fft_battleground.controller.response.model.PlayerData;
import fft_battleground.exception.CacheMissException;
import fft_battleground.exception.TournamentApiException;

public interface PlayerPageDataService {

	PlayerData getDataForPlayerPage(String playerName, TimeZone timezone)
			throws CacheMissException, TournamentApiException;

}