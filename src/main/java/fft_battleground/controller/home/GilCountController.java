package fft_battleground.controller.home;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.response.model.GilDateGraphEntry;
import fft_battleground.dump.model.GlobalGilPageData;
import fft_battleground.dump.service.GlobalGilService;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.repository.GlobalGilHistoryRepo;
import fft_battleground.util.GenericResponse;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class GilCountController extends AbstractHomeController {

	@Autowired
	private GlobalGilService globalGilService;
	
	@Autowired
	private GlobalGilHistoryRepo globalGilHistoryRepo;
	
	@Autowired
	public GilCountController(AccessTracker accessTracker) {
		super(accessTracker);
	}
	
	@ApiIgnore
	@GetMapping("/gilCount")
	public @ResponseBody ResponseEntity<GenericResponse<GlobalGilPageData>> gilCountPage(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("global gil count", userAgent, request);
		GlobalGilPageData data = this.globalGilService.getGlobalGilData();
		model.addAttribute("globalGilData", data);
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@ApiIgnore
	@GetMapping("/globalGilHistoryGraphData")
	public ResponseEntity<GenericResponse<List<GilDateGraphEntry>>> 
	getGlobalGilHistoryGraphData(@RequestParam("timeUnit") String unit) {
		List<GilDateGraphEntry> results = null;
		ChronoUnit timeUnit = null;
		if(StringUtils.equalsIgnoreCase(unit, "day")) {
			timeUnit = ChronoUnit.DAYS;
		} else if(StringUtils.equalsIgnoreCase(unit, "week")) {
			timeUnit = ChronoUnit.WEEKS;
		} else if(StringUtils.equalsIgnoreCase(unit, "month")) {
			timeUnit = ChronoUnit.MONTHS;
		} else if(StringUtils.equalsIgnoreCase(unit, "year")) {
			timeUnit = ChronoUnit.YEARS;
		}
		
		if(timeUnit != null) {
			List<GlobalGilHistory> globalGilHistoryList = this.globalGilHistoryRepo.getGlobalGilHistoryByCalendarTimeType(timeUnit);
			Collections.sort(globalGilHistoryList);
			results = globalGilHistoryList.parallelStream().map(globalGilHistory -> new GilDateGraphEntry(globalGilHistory.getGlobal_gil_count(), globalGilHistory.getDate()))
					.collect(Collectors.toList());
		}
		
		return GenericResponse.createGenericResponseEntity(results);
	}

}
