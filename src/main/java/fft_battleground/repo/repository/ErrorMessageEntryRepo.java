package fft_battleground.repo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fft_battleground.repo.model.ErrorMessageEntry;

@Repository
public interface ErrorMessageEntryRepo extends JpaRepository<ErrorMessageEntry, Long> {

}
