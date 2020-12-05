package fft_battleground.controller;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import fft_battleground.repo.model.ErrorMessageEntry;
import fft_battleground.repo.repository.ErrorMessageEntryRepo;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ErrorMessageController {

	@Autowired
	private ErrorMessageEntryRepo errorMessageEntryRepo;
	
	@GetMapping("/error/stacktrace/{id}")
	@Transactional
	public ResponseEntity<String> getStrackTraceData(@PathVariable Long id) {
		ErrorMessageEntry entry = this.errorMessageEntryRepo.getOne(id);
		String result = entry.getStackTrace();
		return new ResponseEntity<String>(result, HttpStatus.OK);
	}
}
