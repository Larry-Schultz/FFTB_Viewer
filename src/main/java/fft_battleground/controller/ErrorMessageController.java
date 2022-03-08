package fft_battleground.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import fft_battleground.repo.model.ErrorMessageEntry;
import fft_battleground.repo.repository.ErrorMessageEntryRepo;
import fft_battleground.util.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@Slf4j
@ApiIgnore
public class ErrorMessageController {

	@Autowired
	private ErrorMessageEntryRepo errorMessageEntryRepo;
	
	@GetMapping("/error/stacktrace/{id}")
	@Transactional
	public ResponseEntity<GenericResponse<String>> getStrackTraceData(@PathVariable Long id) {
		ErrorMessageEntry entry = this.errorMessageEntryRepo.getOne(id);
		String result = entry.getStackTrace();
		return GenericResponse.createGenericResponseEntity(result);
	}
}
