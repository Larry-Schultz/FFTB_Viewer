package fft_battleground.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import fft_battleground.discord.WebhookManager;
import fft_battleground.exception.BattleGroundException;
import fft_battleground.exception.CacheMissException;

@ControllerAdvice
public class ExceptionController {

	@Autowired
	private WebhookManager noisyWebhookManager;
	
	@ExceptionHandler(BattleGroundException.class)
	public String BattleGroundExceptionController(Model model, BattleGroundException e) {
		this.noisyWebhookManager.sendException(e, "error accessing controllers");
		return "500Error.html";
	}
	
	@ExceptionHandler(CacheMissException.class)
	public String BattleGroundExceptionController(Model model, CacheMissException e) {
		this.noisyWebhookManager.sendException(e, "error accessing cache");
		String cacheName = e.getKey().toString();
		model.addAttribute("cacheName", cacheName);
		return "cacheNotReady.html";
	}
	
}
