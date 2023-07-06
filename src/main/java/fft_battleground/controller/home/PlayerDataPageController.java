package fft_battleground.controller.home;

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.response.model.PlayerData;
import fft_battleground.dump.service.PlayerPageDataService;
import fft_battleground.exception.CacheMissException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.util.GenericResponse;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class PlayerDataPageController extends AbstractHomeController {

	@Autowired
	private PlayerPageDataService playerDataUtil;
	
	@Autowired
	public PlayerDataPageController(AccessTracker accessTracker) {
		super(accessTracker);
	}
	
	@ApiIgnore
	@GetMapping({"/player", "/player/"})
	public @ResponseBody ResponseEntity<GenericResponse<PlayerData>> playerDataPage(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("player search page", userAgent, request);
		PlayerData data = null;
		return GenericResponse.createGenericResponseEntity("No player provided", data);
	}

	@ApiOperation(value="gets all data tied to a player in the Viewer database")
	@GetMapping({"/player/{playerName}"})
	public @ResponseBody ResponseEntity<GenericResponse<PlayerData>> playerDataPage(@PathVariable(name="playerName") String playerName, 
			@RequestParam(name="refresh", required=false, defaultValue="false") Boolean refresh, 
			@RequestHeader(value = "User-Agent", required=false, defaultValue="") String userAgent, 
			Model model, TimeZone timezone, HttpServletRequest request) throws CacheMissException, TournamentApiException {
		if(!refresh) {
			this.logAccess(playerName + " search page ", userAgent, request);
		}
		
		PlayerData playerData = this.playerDataUtil.getDataForPlayerPage(playerName, timezone);
		return GenericResponse.createGenericResponseEntity(playerData);
	}

}
