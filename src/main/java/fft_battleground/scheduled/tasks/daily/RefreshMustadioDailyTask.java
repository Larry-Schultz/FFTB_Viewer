package fft_battleground.scheduled.tasks.daily;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.dump.DumpService;
import fft_battleground.mustadio.MustadioService;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RefreshMustadioDailyTask extends DumpDailyScheduledTask {
	
	@Autowired
	private MustadioService mustadioServiceRef;
	
	public RefreshMustadioDailyTask(@Autowired DumpService dumpService) {
		super(dumpService);
	}

	@Override
	protected void task() {
		log.info("Starting batch refresh of Mustadio data");
		this.mustadioServiceRef.refreshMustadioData();
		log.info("Batch refresh of Mustadio data complete");
	}

}
