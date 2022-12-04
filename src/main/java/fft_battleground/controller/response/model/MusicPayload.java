package fft_battleground.controller.response.model;

import java.util.Collection;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicPayload {
	private Collection<MusicData> musicData;
	private Date firstOccurence;
}
