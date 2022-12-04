package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.HypeEvent;
import fft_battleground.event.model.Hype;
import fft_battleground.model.ChatMessage;

public class HypeDetector implements EventDetector<HypeEvent> {

	@Override
	public HypeEvent detect(ChatMessage message) {
		 HypeEvent event = null;
		 if(!StringUtils.equals(message.getUsername(), "fftbattleground") && !StringUtils.startsWith(message.getMessage(), "!")) {
			 StringTokenizer st = new StringTokenizer(message.getMessage());
			 
			 List<Hype> hypeEmotes = new ArrayList<>();
			 while(st.hasMoreTokens()) {
				 String token = st.nextToken();
				 Hype hype = Hype.getHypeByString(token);
				 if(hype != null) {
					 hypeEmotes.add(hype);
				 }
			 }
			 
			 if(hypeEmotes.size() > 0) {
				 String username = StringUtils.lowerCase(message.getUsername());
				 event = new HypeEvent(username, hypeEmotes);
			 }
		 }
		
		 return event;
	}

}
