package fft_battleground.controller.model;

import java.util.Date;

import lombok.Data;

@Data
public class GilDateGraphEntry {
	private Long globalGilCount;
	private Date date;
	
	public GilDateGraphEntry(Long globalGilCount, Date date) {
		this.globalGilCount = globalGilCount;
		this.date = date;
	}
}