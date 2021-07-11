package fft_battleground.botland.personality;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalityResponse {
	private String response;
	private boolean display = false;
}
