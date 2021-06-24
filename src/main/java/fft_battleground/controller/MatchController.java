package fft_battleground.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.repo.model.Match;
import fft_battleground.util.GenericElementOrdering;
import fft_battleground.util.GenericResponse;

import lombok.SneakyThrows;

@Controller
@RequestMapping("/api/matches")
public class MatchController {

	@Autowired
	private WebsocketThread websocketThread;

	@Autowired
	private AccessTracker accessTracker;
	
	@RequestMapping(value= "/health", method = RequestMethod.GET)
	public ResponseEntity<GenericResponse<Boolean>> healthCheck(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		//this.logAccess("health check", userAgent, request);
		Boolean result = true;
		return GenericResponse.createGenericResponseEntity(result);
	}

	@RequestMapping(value = "/matches", method = RequestMethod.GET)
	public ResponseEntity<GenericResponse<List<Match>>> getMatches(
			@RequestParam(name = "first", required = false, defaultValue = "0") Integer firstMatchIndex) {
		List<Match> matches = new ArrayList<>();
		return GenericResponse.createGenericResponseEntity(matches, HttpStatus.OK);
	}

	@RequestMapping(value = "/currentData", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<GenericResponse<List<GenericElementOrdering<BattleGroundEvent>>>> 
	getCurrentMatch(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		List<GenericElementOrdering<BattleGroundEvent>> events = this.websocketThread.getCurrentEventCache();
		return GenericResponse.createGenericResponseEntity(events);
	}

	
	@RequestMapping(value="/RetrieveEventById", method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<GenericResponse<List<GenericElementOrdering<BattleGroundEvent>>>> 
		getEventById(@RequestParam(name="ids", required=true) String ids) {
		String[] idsSplitStrings = StringUtils.split(ids, ","); 
		List<Long> idLongList = Arrays.asList(idsSplitStrings).stream()
				.map(id ->Long.valueOf(id)).sorted().collect(Collectors.toList()); 
		List<GenericElementOrdering<BattleGroundEvent>> elements = idLongList.stream().map(id -> this.websocketThread.getEventByOrderId(id))
				.sorted().collect(Collectors.toList());
		
		return GenericResponse.createGenericResponseEntity(elements);
	
	}
	
	@SneakyThrows
	protected void logAccess(String pageName, String userAgent, HttpServletRequest request) {
		this.accessTracker.addAccessEntry(pageName, userAgent, request);
	}
	 
}

class BattleGroundEventComparator implements Comparator<BattleGroundEvent> {

	@Override
	public int compare(BattleGroundEvent o1, BattleGroundEvent o2) {
		int priority1 = this.getPriority(o1.getEventType());
		int priority2 = this.getPriority(o2.getEventType());

		if (priority1 == priority2) {
			int eventTimeCompareResult = o1.getEventTime().compareTo(o2.getEventTime());
			return eventTimeCompareResult;
		} else if (priority1 > priority2) {
			return 1;
		} else {
			return -1;
		}
	}

	protected int getPriority(BattleGroundEventType type) {
		int priority = Integer.MAX_VALUE;
		switch (type) {
		case BETTING_BEGINS:
			priority = 0;
			break;
		case FIGHT_BEGINS:
			priority = 1;
			break;
		case BET:
		case BAD_BET:
			priority = 5;
			break;
		case MATCH_INFO:
			priority = 4;
			break;
		case TEAM_INFO:
			priority = 2;
			break;
		case UNIT_INFO:
			priority = 3;
			break;
		}

		return priority;

	}

}
