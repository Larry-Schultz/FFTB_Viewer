package fft_battleground.controller.home;

import javax.servlet.http.HttpServletRequest;

import fft_battleground.metrics.AccessTracker;
import lombok.SneakyThrows;

public abstract class AbstractHomeController {

	private AccessTracker accessTracker;
	
	public AbstractHomeController(AccessTracker accessTracker) {
		this.accessTracker = accessTracker;
	}
	
	@SneakyThrows
	protected void logAccess(String pageName, String userAgent, HttpServletRequest request) {
		this.accessTracker.addAccessEntry(pageName, userAgent, request);
	}
}
