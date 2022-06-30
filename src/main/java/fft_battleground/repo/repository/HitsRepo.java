package fft_battleground.repo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fft_battleground.repo.model.Hits;
import fft_battleground.repo.util.HitsType;

@Repository
public interface HitsRepo extends JpaRepository<Hits, String> {
	
	public default Hits incrementTodaysHits(HitsType type) {
		String id = Hits.getIdStringForToday(type);
		Optional<Hits> maybeHits = this.findById(id);
		if(maybeHits.isPresent()) {
			maybeHits.get().incrementByOne();
			this.saveAndFlush(maybeHits.get());
			return maybeHits.get();
		} else {
			Hits newHits = new Hits(type);
			this.saveAndFlush(newHits);
			return newHits;
		}
		
		
	}
	
}
