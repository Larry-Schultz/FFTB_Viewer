package fft_battleground.bot.controller;

import java.util.List;

import javax.websocket.server.PathParam;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.model.ChatMessage;
import fft_battleground.util.GenericResponse;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/chat")
@Slf4j
public class ChatMessageController {

	@Autowired
	private Router<ChatMessage> messageSenderRouter;
	
	/*
	 * @Value("${apiKey}") private String apiKey;
	 */
	
	@PostMapping("/messages/{key}")
	public @ResponseBody ResponseEntity<GenericResponse<Integer>>
	receiveChatMessages(@RequestBody List<ChatMessage> messages, @PathParam("key") String key) {
		this.messageSenderRouter.sendAllDataToQueues(messages);
		log.info("received messages: {}", messages);
		return GenericResponse.<Integer>createGenericResponseEntity(null, "success", HttpStatus.OK);
		
	}
	
}
