package fft_battleground.irc;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import fft_battleground.model.ChatMessage;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwitchChatListenerAdapter extends ListenerAdapter {

    private Router<ChatMessage> chatMessageRouter;
    private String channel;

    public TwitchChatListenerAdapter() {
        super();
    }

    public TwitchChatListenerAdapter(Router<ChatMessage> chatMessageRouter, String channel) {
        super();
        this.chatMessageRouter = chatMessageRouter;
        this.channel = channel;
    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        // this.sentenceGenerator.addString(event.getMessage());
        // log.info("message: " + event.getMessage());
        this.chatMessageRouter
                .sendDataToQueues(new ChatMessage(this.channel, event.getUser().getNick(), event.getMessage()));
    }

}