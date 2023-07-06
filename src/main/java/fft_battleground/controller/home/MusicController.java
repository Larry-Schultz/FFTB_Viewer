package fft_battleground.controller.home;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.response.model.MusicData;
import fft_battleground.controller.response.model.MusicPayload;
import fft_battleground.exception.CacheMissException;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.music.MusicService;
import fft_battleground.music.model.Music;
import fft_battleground.reports.PlaylistSongCountHistoryReportGenerator;
import fft_battleground.reports.PlaylistSongOccurenceHistoryReportGenerator;
import fft_battleground.reports.model.LeaderboardBalanceData;
import fft_battleground.util.GenericResponse;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping("/music")
public class MusicController extends AbstractHomeController {

	@Autowired
	private MusicService musicService;
	
	@Autowired
	private PlaylistSongCountHistoryReportGenerator playlistSongCountHistoryReportGenerator;
	
	@Autowired
	private PlaylistSongOccurenceHistoryReportGenerator playlistSongOccurenceHistoryReportGenerator;
	
	@Autowired
	public MusicController(AccessTracker accessTracker) {
		super(accessTracker);
		// TODO Auto-generated constructor stub
	}
	
	@ApiOperation(value="returns all songs in the stream's playlist")
	@GetMapping("")
	public @ResponseBody ResponseEntity<GenericResponse<MusicPayload>> musicPage(@RequestHeader(value = "User-Agent", required=false, defaultValue="") String userAgent, 
			Model model, HttpServletRequest request) throws CacheMissException {
		this.logAccess("music search page", userAgent, request);
		Collection<Music> music = this.musicService.getPlaylist();
		if(music == null || music.size() == 0) {
			throw new CacheMissException("No cache data for music playlist!");
		}
		Collection<MusicData> data = music.parallelStream().map(musicEntry -> new MusicData(musicEntry)).collect(Collectors.toList());
		Date firstOccurence = this.musicService.getFirstOccurenceDate();
		MusicPayload payload = new MusicPayload(data, firstOccurence);
		return GenericResponse.createGenericResponseEntity(payload);
	}
	
	@ApiIgnore
	@GetMapping("/songCount")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>> songCount() throws CacheMissException {
		LeaderboardBalanceData report = this.playlistSongCountHistoryReportGenerator.getReport();
		return GenericResponse.createGenericResponseEntity(report);
	}
	
	@ApiIgnore
	@GetMapping("/songOccurencesCount")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>> songOccurencesCount() throws CacheMissException {
		LeaderboardBalanceData report = this.playlistSongOccurenceHistoryReportGenerator.getReport();
		return GenericResponse.createGenericResponseEntity(report);
	}

}
