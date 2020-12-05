package fft_battleground.repo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fft_battleground.repo.model.ErrorMessageEntry;

public interface ErrorMessageEntryRepo extends JpaRepository<ErrorMessageEntry, Long> {

}
