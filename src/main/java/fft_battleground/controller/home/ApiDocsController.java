package fft_battleground.controller.home;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import fft_battleground.metrics.AccessTracker;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class ApiDocsController extends AbstractHomeController {

	@Autowired
	public ApiDocsController(AccessTracker accessTracker) {
		super(accessTracker);
	}
	
	@ApiIgnore
	@GetMapping("/apidocs")
	public String apiDocsPage(@RequestHeader(value = "User-Agent") String userAgent, Model Model, HttpServletRequest request) {
		this.logAccess("apidocs", userAgent, request);
		return "api.html";
	}

}
