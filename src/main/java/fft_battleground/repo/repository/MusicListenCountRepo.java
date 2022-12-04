package fft_battleground.repo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fft_battleground.repo.model.MusicListenCount;

@Repository
public interface MusicListenCountRepo extends JpaRepository<MusicListenCount, Long> {

	@Query("SELECT SUM(mlc.occurences) FROM MusicListenCount mlc")
	public int sumMusicListens();
	
}
