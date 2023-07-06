package fft_battleground.controller.home;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.metrics.AccessTracker;
import fft_battleground.reports.PrestigeTableReportGenerator;
import fft_battleground.reports.model.AscensionData;
import fft_battleground.util.GenericResponse;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class AscensionController extends AbstractHomeController {

	@Autowired
	private PrestigeTableReportGenerator prestigeTableReportGenerator;
	
	@Autowired
	public AscensionController(AccessTracker accessTracker) {
		super(accessTracker);
	}
	
	@ApiIgnore
	@ApiOperation("returns the list of ascended players and related data")
	@GetMapping("/ascension")
	@SneakyThrows
	public @ResponseBody ResponseEntity<GenericResponse<AscensionData>> ascension(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("ascension", userAgent, request);
		AscensionData prestigeEntries = this.prestigeTableReportGenerator.generateReport();
		
		Date generationDate = new Date();
		String generationDateFormatString = "yyyy-MM-dd hh:mm:ss aa zzz";
		SimpleDateFormat sdf = new SimpleDateFormat(generationDateFormatString);
		String generationDateString = sdf.format(generationDate);
		
		prestigeEntries.setGenerationDateString(generationDateString);
		
		return GenericResponse.createGenericResponseEntity(prestigeEntries);
	}

}
