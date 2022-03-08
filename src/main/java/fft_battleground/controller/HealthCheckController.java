package fft_battleground.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class HealthCheckController {

	@Autowired
	private WebsocketThread websocketThread;
	
	@GetMapping("/health")
	public ResponseEntity<String> healthCheck() {
		return new ResponseEntity<String>("success", HttpStatus.OK);
		
	}
	
	@GetMapping("/health/update")
	public ResponseEntity<String> lastUpdateDate() {
		Date lastUpdateDate = this.websocketThread.lastUpdateDate();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy HH:mm:ss z");
		String dateString = lastUpdateDate != null ? sdf.format(lastUpdateDate) : "null";
		HttpStatus status = lastUpdateDate != null ? HttpStatus.OK : HttpStatus.GONE;
		return new ResponseEntity<String>(dateString, status);
	}
}
