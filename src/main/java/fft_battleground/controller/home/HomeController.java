package fft_battleground.controller.home;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import fft_battleground.metrics.AccessTracker;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping("/")
@Slf4j
public class HomeController extends AbstractHomeController {
	
	@Autowired
	public HomeController(AccessTracker accessTracker) {
		super(accessTracker);
		// TODO Auto-generated constructor stub
	}
	
	@ApiIgnore
	@GetMapping("/")
	public String homePage(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("index page", userAgent, request);
		return "index.html";
	}
	
}
