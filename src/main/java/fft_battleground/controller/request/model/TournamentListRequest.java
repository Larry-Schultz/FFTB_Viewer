package fft_battleground.controller.request.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentListRequest {
	public List<Long> tournamentIds;
}
