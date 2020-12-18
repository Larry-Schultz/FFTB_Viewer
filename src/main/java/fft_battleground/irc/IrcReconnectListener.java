package fft_battleground.irc;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IrcReconnectListener extends RetryListenerSupport {

	@Getter private int currentReconnectCount = 0;
	
	public void clearCount() {
		this.currentReconnectCount = 0;
	}
	
	@Override
    public <T, E extends Throwable> void close(RetryContext context,
                                               RetryCallback<T, E> callback, Throwable throwable) {

        log.error("Unable to recover from  Exception");
        log.error("Error ", throwable);
        super.close(context, callback, throwable);
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.error("IRC Reconnect Exception Occurred, Retry Count {} ", context.getRetryCount());
        this.currentReconnectCount = context.getRetryCount();
        super.onError(context, callback, throwable);
    }

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context,
                                                 RetryCallback<T, E> callback) {
        log.error("Exception Occurred, IRC Retry Session Started ");
        return super.open(context, callback);
    }
}
