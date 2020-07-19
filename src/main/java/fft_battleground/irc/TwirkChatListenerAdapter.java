package fft_battleground.irc;

import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;

import fft_battleground.model.ChatMessage;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwirkChatListenerAdapter implements TwirkListener {
	
    private Router<ChatMessage> chatMessageRouter;
    private String channel;
    
    public TwirkChatListenerAdapter(Router<ChatMessage> chatMessageRouter, String channel) {
    	this.chatMessageRouter = chatMessageRouter;
    	this.channel = channel;
    }
	
	@Override
	public void onPrivMsg( TwitchUser sender, TwitchMessage message) {
		this.chatMessageRouter.sendDataToQueues(new ChatMessage(this.channel, sender.getUserName(), message.getContent(), sender.isSub()));
	}

}
