package fft_battleground.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.botland.BetBotFactory;
import fft_battleground.botland.bot.exception.BotConfigException;
import fft_battleground.botland.bot.genetic.GeneFileCache;
import fft_battleground.botland.bot.genetic.model.GeneTrainerV2BotData;
import fft_battleground.botland.bot.genetic.model.ResultData;
import fft_battleground.dump.reports.BotlandLeaderboardReportGenerator;
import fft_battleground.dump.reports.model.BotlandLeaderboard;
import fft_battleground.dump.reports.model.BotlandLeaderboardEntry;
import fft_battleground.exception.CacheMissException;
import fft_battleground.util.GenericResponse;
import springfox.documentation.annotations.ApiIgnore;

@RequestMapping("/")
@Controller
public class BotlandController {

	@Autowired
	private BetBotFactory betBotFactory;
	
	@Autowired
	private BotlandLeaderboardReportGenerator botlandLeaderboardReportGenerator;
	
	@GetMapping("/botlandleaderboard")
	@ApiIgnore
	public ResponseEntity<GenericResponse<BotlandLeaderboard>> getBotlandLeaderboard() throws CacheMissException {
		return GenericResponse.createGenericResponseEntity(this.botlandLeaderboardReportGenerator.getReport());
	}
	
	@GetMapping("/botlandleaderboard/{botName}")
	@ApiIgnore
	public ResponseEntity<GenericResponse<BotlandLeaderboardEntry>> getBotlandLeaderboard(@PathVariable("botName") String botName) throws CacheMissException {
		return GenericResponse.createGenericResponseEntity(this.botlandLeaderboardReportGenerator.getReport().getLeaderboardEntryForPlayer(botName));
	}
	
	@GetMapping("/genefile/{genefile}")
	public ResponseEntity<String> getGeneFile(@PathVariable("genefile") String genefile) throws BotConfigException, JsonProcessingException {
		GeneFileCache<ResultData> v1Cache = this.betBotFactory.getGeneFileCache();
		GeneFileCache<GeneTrainerV2BotData> v2Cache = this.betBotFactory.getGeneFileV2Cache();
		if(v1Cache.hasFile(genefile)) {
			ResultData genefileData = v1Cache.getGeneData(genefile);
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(genefileData);
			return new ResponseEntity<String>(json, HttpStatus.OK);
		} else if(v2Cache.hasFile(genefile)) {
			GeneTrainerV2BotData data = v2Cache.getGeneData(genefile);
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(data);
			return new ResponseEntity<String>(json, HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
		}
	}
}
