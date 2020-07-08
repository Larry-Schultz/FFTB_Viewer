package fft_battleground.botland.model;

import java.util.Map;

import lombok.Data;

@Data
public class BotData {
	private String name;
	private String classname;
	private boolean canPrimary;
	private Map<String, String> params;
	
	public BotData() {}
	
	public BotData(String name, String classname, Map<String, String> params) {
		this.name = name;
		this.classname = classname;
		this.params = params;
		
		this.canPrimary = true;
	}
	
	public BotData(String name, String classname, Map<String, String> params, boolean qualifierForPrimary) {
		this.name = name;
		this.classname = classname;
		this.params = params;
		
		this.canPrimary = true;
	}
}