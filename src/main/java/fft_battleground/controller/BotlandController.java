package fft_battleground.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fft_battleground.repo.repository.BotsHourlyDataRepo;
import fft_battleground.repo.repository.BotsRepo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/botland")
@Slf4j
public class BotlandController {
	
}

@Data
@AllArgsConstructor
class HourGilGraphEntry {
	private String hour;
	private Long gil;
}
