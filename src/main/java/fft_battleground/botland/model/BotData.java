package fft_battleground.botland.model;

import java.util.Map;

import lombok.Data;

@Data
public class BotData {
	private String name;
	private String classname;
	private boolean canPrimary;
	private String description;
	private Map<String, BotParam> params;
	
	public BotData() {}
	
	public BotData(String name, String classname, Map<String, BotParam> params, String description) {
		this.name = name;
		this.classname = classname;
		this.params = params;
		this.description = description;
		
		this.canPrimary = true;
	}
	
	public BotData(String name, String classname, Map<String, BotParam> params, boolean qualifierForPrimary, String description) {
		this.name = name;
		this.classname = classname;
		this.params = params;
		this.description = description;
		this.canPrimary = true;
	}
}