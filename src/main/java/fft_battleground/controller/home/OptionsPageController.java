package fft_battleground.controller.home;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.metrics.AccessTracker;
import fft_battleground.util.GenericResponse;

@Controller
@RequestMapping("/pages")
public class OptionsPageController extends AbstractHomeController {

	private static final String PAGE_NAME = "options";
	
	@Autowired
	public OptionsPageController(AccessTracker accessTracker) {
		super(accessTracker);
	}
	
	@GetMapping("/options")
	public @ResponseBody ResponseEntity<GenericResponse<String>> reportOptionsPageOpen(@RequestHeader(value = "User-Agent", required=false, defaultValue="") String userAgent, 
			Model model, HttpServletRequest request) {
		this.logAccess(PAGE_NAME, userAgent, request);
		return GenericResponse.createGenericResponseEntity("OK");
	}

}
