package fft_battleground.repo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fft_battleground.repo.model.MusicListenCount;

public interface MusicListenCountRepo extends JpaRepository<MusicListenCount, Long> {

}