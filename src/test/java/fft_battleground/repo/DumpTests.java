package fft_battleground.repo;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fft_battleground.botland.SecondaryBotConfig;
import fft_battleground.botland.model.BotData;
import fft_battleground.dump.DumpDataProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class DumpTests {

	@Test
	public void testBotlandLoad() {
		SecondaryBotConfig config = new SecondaryBotConfig("Botland.xml");
		List<BotData> botData = config.parseXmlFile();
		assertTrue(config != null && botData != null);
		assertTrue(botData.size() > 0);
	}
		
	
}
